/* ******************************************************************************
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

package tools.vitruv.dsls.vitruvocl.pipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  private static final String PKG_CORRESPONDENCE = "correspondence";

  private SmartLoader() {}

  /** Result of loading operation with wrapper, errors, and warnings. */
  public static class LoadResult {
    /** Configured wrapper with loaded metamodels and instances. */
    public final MetamodelWrapper wrapper;

    /** File-level errors preventing loading. */
    public final List<FileError> fileErrors;

    /** Non-fatal warnings about unused or duplicate files. */
    public final List<Warning> warnings;

    /**
     * Creates a load result.
     *
     * @param wrapper configured wrapper with loaded metamodels and instances
     * @param fileErrors file-level errors preventing loading
     * @param warnings non-fatal warnings about unused or duplicate files
     */
    public LoadResult(
        MetamodelWrapper wrapper, List<FileError> fileErrors, List<Warning> warnings) {
      this.wrapper = wrapper;
      this.fileErrors = fileErrors;
      this.warnings = warnings;
    }

    /**
     * Returns whether any file errors occurred.
     *
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
  public static LoadResult loadForConstraint(
      String constraint, Path[] ecoreFiles, Path[] xmiFiles) {
    MetamodelWrapper wrapper = new MetamodelWrapper();
    List<FileError> fileErrors = new ArrayList<>();
    List<Warning> warnings = new ArrayList<>();

    List<Path> resolvedEcores = resolveAndValidate(ecoreFiles, fileErrors);
    List<Path> resolvedXmis = resolveAndValidate(xmiFiles, fileErrors);
    if (!fileErrors.isEmpty()) {
      return new LoadResult(wrapper, fileErrors, warnings);
    }

    // Resolve platform:/plugin/ supertype references (e.g. PCM's Identifier/Units ecores) to
    // local copies among the supplied ecore files, so cross-ecore eSuperTypes are visible to
    // EMF outside of an Eclipse/OSGi runtime. Without this, types like pcm's Entity (which
    // extends platform:/plugin/de.uka.ipd.sdq.identifier/model/identifier.ecore#//Identifier)
    // fail to load model instances referencing inherited features (e.g. "id").
    wrapper.registerWorkspaceEcoresForPlatformResolution(resolvedEcores);

    Map<String, Path> availableEcores = mapEcoresToPackageNames(resolvedEcores, fileErrors);
    XmiIndex xmiIndex = indexXmiPackageNames(resolvedXmis, warnings);

    if (!fileErrors.isEmpty()) {
      return new LoadResult(wrapper, fileErrors, warnings);
    }

    Set<String> requiredPackages = DependencyAnalyzer.analyzeConstraint(constraint);
    // Always load correspondence instances when present — the ecore is embedded in the JAR
    // and auto-registered, so no explicit .ecore file is required for it.
    requiredPackages.add(PKG_CORRESPONDENCE);

    loadRequiredPackages(
        wrapper, requiredPackages, availableEcores, xmiIndex, fileErrors, warnings);

    return new LoadResult(wrapper, fileErrors, warnings);
  }

  /** Resolves and validates each path, collecting file-level errors as they are found. */
  private static List<Path> resolveAndValidate(Path[] paths, List<FileError> fileErrors) {
    List<Path> resolved = new ArrayList<>();
    for (Path path : paths) {
      Path resolvedPath = resolveFilePath(path);
      FileValidator.validateFile(resolvedPath).ifPresent(fileErrors::add);
      resolved.add(resolvedPath);
    }
    return resolved;
  }

  /** Maps each ecore file to every package name it declares (first file wins on conflicts). */
  private static Map<String, Path> mapEcoresToPackageNames(
      List<Path> resolvedEcores, List<FileError> fileErrors) {
    Map<String, Path> availableEcores = new HashMap<>();
    for (Path ecore : resolvedEcores) {
      try {
        Set<String> packageNames = FileValidator.extractAllPackageNamesFromEcore(ecore);
        for (String packageName : packageNames) {
          availableEcores.putIfAbsent(packageName, ecore);
        }
      } catch (IOException e) {
        fileErrors.add(new FileError(ecore, FileError.FileErrorType.PARSE_ERROR, e.getMessage()));
      }
    }
    return availableEcores;
  }

  /**
   * One entry per occurrence in the xmi file list, each carrying every package name referenced by
   * that file. Duplicated input paths are kept as separate entries so that callers intentionally
   * passing the same file twice get it loaded twice.
   */
  private record XmiIndex(List<Path> occurrences, List<Set<String>> packageNames) {}

  /** Extracts package names referenced by each xmi file, skipping unrecognised files. */
  private static XmiIndex indexXmiPackageNames(List<Path> resolvedXmis, List<Warning> warnings) {
    List<Path> occurrences = new ArrayList<>();
    List<Set<String>> packageNames = new ArrayList<>();
    for (Path xmi : resolvedXmis) {
      try {
        packageNames.add(FileValidator.extractAllPackageNamesFromXmi(xmi));
        occurrences.add(xmi);
      } catch (IOException e) {
        // Non-XML or unrecognised files (e.g. Vitruvius-internal metadata) are skipped silently.
        warnings.add(
            new Warning(
                Warning.WarningType.UNUSED_MODEL,
                "Skipping unrecognised instance file: " + xmi.getFileName()));
      }
    }
    return new XmiIndex(occurrences, packageNames);
  }

  /** Loads only required metamodels and the instances that match them. */
  private static void loadRequiredPackages(
      MetamodelWrapper wrapper,
      Set<String> requiredPackages,
      Map<String, Path> availableEcores,
      XmiIndex xmiIndex,
      List<FileError> fileErrors,
      List<Warning> warnings) {
    boolean[] loaded = new boolean[xmiIndex.occurrences().size()];
    for (String pkg : requiredPackages) {
      loadMetamodelForPackage(wrapper, pkg, availableEcores, fileErrors);
      boolean foundInstance = loadInstancesForPackage(wrapper, pkg, xmiIndex, loaded, fileErrors);

      // Don't warn for correspondence package if it has no instances
      if (!foundInstance && !pkg.equals(PKG_CORRESPONDENCE)) {
        warnings.add(
            new Warning(Warning.WarningType.UNUSED_MODEL, "No instances for '" + pkg + "'"));
      }
    }
  }

  private static void loadMetamodelForPackage(
      MetamodelWrapper wrapper,
      String pkg,
      Map<String, Path> availableEcores,
      List<FileError> fileErrors) {
    if (!availableEcores.containsKey(pkg)) {
      // PKG_CORRESPONDENCE ecore is embedded in the JAR and auto-registered — not an error.
      if (!pkg.equals(PKG_CORRESPONDENCE)) {
        fileErrors.add(
            new FileError(
                null,
                FileError.FileErrorType.NOT_FOUND,
                "Required metamodel '" + pkg + "' not found"));
      }
      return;
    }
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

  private static boolean loadInstancesForPackage(
      MetamodelWrapper wrapper,
      String pkg,
      XmiIndex xmiIndex,
      boolean[] loaded,
      List<FileError> fileErrors) {
    boolean foundInstance = false;
    List<Path> occurrences = xmiIndex.occurrences();
    List<Set<String>> packageNames = xmiIndex.packageNames();
    for (int i = 0; i < occurrences.size(); i++) {
      if (!packageNames.get(i).contains(pkg)) {
        continue;
      }
      foundInstance = true;
      if (!loaded[i]) {
        // Already loaded for a different package name found in the same occurrence
        // (e.g. a repository:Repository root with nested xsi:type="seff:..." elements).
        loaded[i] = true;
        loadModelInstance(wrapper, occurrences.get(i), fileErrors);
      }
    }
    return foundInstance;
  }

  private static void loadModelInstance(
      MetamodelWrapper wrapper, Path xmi, List<FileError> fileErrors) {
    try {
      wrapper.loadModelInstance(xmi);
    } catch (IOException e) {
      fileErrors.add(
          new FileError(
              xmi, FileError.FileErrorType.PARSE_ERROR, "Failed to load model: " + e.getMessage()));
    } catch (Exception e) {
      fileErrors.add(
          new FileError(
              xmi,
              FileError.FileErrorType.PARSE_ERROR,
              "Failed to load model (runtime error): " + e.getMessage()));
    }
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
    Path testPath = MetamodelWrapper.getTestModelsPath().resolve(path);
    if (Files.exists(testPath)) {
      return testPath;
    }

    // Fallback to absolute from current working directory
    return path.toAbsolutePath();
  }
}
