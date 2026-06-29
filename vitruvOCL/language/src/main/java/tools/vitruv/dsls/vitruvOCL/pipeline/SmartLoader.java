/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.pipeline;
import java.util.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Intelligent loader that analyzes constraints to load only required metamodels and instances.
 *
 * <p>Performs dependency analysis on constraints to determine which metamodels are actually
 * referenced, then selectively loads only necessary files. Provides comprehensive error reporting
 * and warnings for missing dependencies, unused files, and duplicate definitions.
 *
 * <p>Handles file resolution with fallback logic: absolute paths used as-is, relative paths
 * resolved via TEST_MODELS_PATH then current working directory.
 */
public class SmartLoader {

  private static final Logger LOG = Logger.getLogger(SmartLoader.class.getName());

  private static final String PKG_CORRESPONDENCE = "correspondence";

  private SmartLoader() {}

  /** Result of loading operation with wrapper, errors, and warnings. */
  public static class LoadResult {
    /** Configured wrapper with loaded metamodels and instances */
    public final MetamodelWrapper wrapper;

    /** File-level errors preventing loading */
    public final List<FileError> fileErrors;

    /** Non-fatal warnings about unused or duplicate files */
    public final List<Warning> warnings;

    public LoadResult(
        MetamodelWrapper wrapper, List<FileError> fileErrors, List<Warning> warnings) {
      this.wrapper = wrapper;
      this.fileErrors = fileErrors;
      this.warnings = warnings;
    }

    /**
     * @return {@code true} if any file errors occurred
     */
    public boolean hasErrors() {
      return !fileErrors.isEmpty();
    }
  }

  /**
   * Loads metamodels and instances based on constraint dependencies.
   *
   * <p>Process:
   *
   * <ol>
   *   <li>Analyzes constraint to extract required package names
   *   <li>Validates and resolves all file paths
   *   <li>Maps files to package names via introspection
   *   <li>Loads only metamodels referenced in constraint
   *   <li>Loads corresponding model instances
   *   <li>Warns about unused files and missing instances
   * </ol>
   *
   * @param constraint OCL constraint expression to analyze
   * @param ecoreFiles Metamodel files (absolute or relative paths)
   * @param xmiFiles Model instance files (absolute or relative paths)
   * @return Load result with configured wrapper, errors, and warnings
   */
  @SuppressWarnings("java:S3776")
  public static LoadResult loadForConstraint(
      String constraint, Path[] ecoreFiles, Path[] xmiFiles) {
    MetamodelWrapper wrapper = new MetamodelWrapper();
    List<FileError> fileErrors = new ArrayList<>();
    List<Warning> warnings = new ArrayList<>();

    Set<String> requiredPackages = DependencyAnalyzer.analyzeConstraint(constraint);

    // Validate and resolve all files
    List<Path> resolvedEcores = new ArrayList<>();
    for (Path ecore : ecoreFiles) {
      Path resolved = resolveFilePath(ecore);
      FileValidator.validateFile(resolved).ifPresent(fileErrors::add);
      resolvedEcores.add(resolved);
    }

    List<Path> resolvedXmis = new ArrayList<>();
    for (Path xmi : xmiFiles) {
      Path resolved = resolveFilePath(xmi);
      FileValidator.validateFile(resolved).ifPresent(fileErrors::add);
      resolvedXmis.add(resolved);
    }

    if (!fileErrors.isEmpty()) {
      return new LoadResult(wrapper, fileErrors, warnings);
    }

    // Resolve platform:/plugin/ supertype references (e.g. PCM's Identifier/Units ecores) to
    // local copies among the supplied ecore files, so cross-ecore eSuperTypes are visible to
    // EMF outside of an Eclipse/OSGi runtime. Without this, types like pcm's Entity (which
    // extends platform:/plugin/de.uka.ipd.sdq.identifier/model/identifier.ecore#//Identifier)
    // fail to load model instances referencing inherited features (e.g. "id").
    wrapper.registerWorkspaceEcoresForPlatformResolution(resolvedEcores);

    // Map files to package names
    Map<String, Path> availableEcores = new HashMap<>();

    for (Path ecore : resolvedEcores) {
      try {
        Set<String> packageNames = FileValidator.extractAllPackageNamesFromEcore(ecore);
        for (String packageName : packageNames) {
          if (!availableEcores.containsKey(packageName)) {
            availableEcores.put(packageName, ecore);
          }
        }
      } catch (IOException e) {
        fileErrors.add(new FileError(ecore, FileError.FileErrorType.PARSE_ERROR, e.getMessage()));
      }
    }

    // One entry per occurrence in xmiFiles (duplicated input paths are kept as separate entries
    // so that callers intentionally passing the same file twice get it loaded twice), each
    // carrying every package name referenced by that file (a single root element may declare
    // xmlns:xxx for several packages used by deeply nested xsi:type elements).
    List<Path> xmiOccurrences = new ArrayList<>();
    List<Set<String>> xmiPackageNames = new ArrayList<>();
    for (Path xmi : resolvedXmis) {
      try {
        Set<String> packageNames = FileValidator.extractAllPackageNamesFromXmi(xmi);
        xmiOccurrences.add(xmi);
        xmiPackageNames.add(packageNames);
      } catch (IOException e) {
        // Non-XML or unrecognised files (e.g. Vitruvius-internal metadata) are skipped silently.
        warnings.add(new Warning(Warning.WarningType.UNUSED_MODEL,
            "Skipping unrecognised instance file: " + xmi.getFileName()));
      }
    }

    if (!fileErrors.isEmpty()) {
      return new LoadResult(wrapper, fileErrors, warnings);
    }

    // Always load correspondence instances when present — the ecore is embedded in the JAR
    // and auto-registered, so no explicit .ecore file is required for it.
    requiredPackages.add(PKG_CORRESPONDENCE);

    LOG.fine("[DBG-SL] requiredPackages=" + requiredPackages);
    LOG.fine("[DBG-SL] availableEcores=" + availableEcores.keySet());
    LOG.fine("[DBG-SL] xmiPackageNames=" + xmiPackageNames);

    // Load only required metamodels and the instances that match them.
    boolean[] loaded = new boolean[xmiOccurrences.size()];
    for (String pkg : requiredPackages) {
      if (!availableEcores.containsKey(pkg)) {
        // PKG_CORRESPONDENCE ecore is embedded in the JAR and auto-registered — not an error.
        if (!pkg.equals(PKG_CORRESPONDENCE)) {
          fileErrors.add(
              new FileError(
                  null,
                  FileError.FileErrorType.NOT_FOUND,
                  "Required metamodel '" + pkg + "' not found"));
        }
      } else {
        try {
          wrapper.loadMetamodel(pkg, availableEcores.get(pkg));
        } catch (IOException e) {
          fileErrors.add(
              new FileError(
                  availableEcores.get(pkg),
                  FileError.FileErrorType.PARSE_ERROR,
                  "Failed to load metamodel: " + e.getMessage()));
        }
      }

      boolean foundInstance = false;
      for (int i = 0; i < xmiOccurrences.size(); i++) {
        if (!xmiPackageNames.get(i).contains(pkg)) {
          continue;
        }
        foundInstance = true;
        if (loaded[i]) {
          // Already loaded for a different package name found in the same occurrence
          // (e.g. a repository:Repository root with nested xsi:type="seff:..." elements).
          continue;
        }
        loaded[i] = true;
        Path xmi = xmiOccurrences.get(i);
        try {
          LOG.fine("[DBG-SL] Loading XMI: " + xmi.getFileName());
          wrapper.loadModelInstance(xmi);
          LOG.fine("[DBG-SL] XMI loaded OK: " + xmi.getFileName());
        } catch (IOException e) {
          LOG.fine("[DBG-SL] IOException loading " + xmi.getFileName() + ": " + e.getMessage());
          fileErrors.add(
              new FileError(
                  xmi,
                  FileError.FileErrorType.PARSE_ERROR,
                  "Failed to load model: " + e.getMessage()));
        } catch (Exception e) {
          LOG.fine("[DBG-SL] RuntimeException loading " + xmi.getFileName() + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
          fileErrors.add(
              new FileError(
                  xmi,
                  FileError.FileErrorType.PARSE_ERROR,
                  "Failed to load model (runtime error): " + e.getMessage()));
        }
      }

      // Don't warn for correspondence package if it has no instances
      if (!foundInstance && !pkg.equals(PKG_CORRESPONDENCE)) {
        warnings.add(
            new Warning(Warning.WarningType.UNUSED_MODEL, "No instances for '" + pkg + "'"));
      }
    }

    return new LoadResult(wrapper, fileErrors, warnings);
  }

  /**
   * Resolves file path with fallback logic.
   *
   * <p>Resolution order:
   *
   * <ol>
   *   <li>Absolute paths used as-is
   *   <li>Relative paths tried under TEST_MODELS_PATH
   *   <li>Fallback to current working directory
   * </ol>
   *
   * @param path Path to resolve
   * @return Resolved absolute path
   */
  private static Path resolveFilePath(Path path) {
    if (path.isAbsolute()) {
      return path;
    }

    // Try TEST_MODELS_PATH first
    Path testPath = MetamodelWrapper.TEST_MODELS_PATH.resolve(path);
    if (Files.exists(testPath)) {
      return testPath;
    }

    // Fallback to absolute from current working directory
    return path.toAbsolutePath();
  }
}
