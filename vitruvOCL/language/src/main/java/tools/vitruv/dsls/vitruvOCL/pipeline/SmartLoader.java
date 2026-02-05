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

    // Map files to package names
    Map<String, Path> availableEcores = new HashMap<>();
    Map<String, List<Path>> availableXmis = new HashMap<>();

    for (Path ecore : resolvedEcores) {
      try {
        String packageName = FileValidator.extractPackageNameFromEcore(ecore);
        if (availableEcores.containsKey(packageName)) {
          warnings.add(
              new Warning(
                  Warning.WarningType.DUPLICATE_METAMODEL,
                  "Metamodel '" + packageName + "' defined multiple times",
                  ecore));
        } else {
          availableEcores.put(packageName, ecore);
        }
      } catch (IOException e) {
        fileErrors.add(new FileError(ecore, FileError.FileErrorType.PARSE_ERROR, e.getMessage()));
      }
    }

    for (Path xmi : resolvedXmis) {
      try {
        String packageName = FileValidator.extractPackageNameFromXmi(xmi);
        availableXmis.computeIfAbsent(packageName, k -> new ArrayList<>()).add(xmi);
      } catch (IOException e) {
        fileErrors.add(new FileError(xmi, FileError.FileErrorType.PARSE_ERROR, e.getMessage()));
      }
    }

    if (!fileErrors.isEmpty()) {
      return new LoadResult(wrapper, fileErrors, warnings);
    }

    // Load only required metamodels
    for (String pkg : requiredPackages) {
      if (!availableEcores.containsKey(pkg)) {
        fileErrors.add(
            new FileError(
                null,
                FileError.FileErrorType.NOT_FOUND,
                "Required metamodel '" + pkg + "' not found"));
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

      if (availableXmis.containsKey(pkg)) {
        for (Path xmi : availableXmis.get(pkg)) {
          try {
            wrapper.loadModelInstance(xmi);
          } catch (IOException e) {
            fileErrors.add(
                new FileError(
                    xmi,
                    FileError.FileErrorType.PARSE_ERROR,
                    "Failed to load model: " + e.getMessage()));
          }
        }
      } else {
        warnings.add(
            new Warning(Warning.WarningType.UNUSED_MODEL, "No instances for '" + pkg + "'"));
      }
    }

    // Warn about unused files
    for (String pkg : availableEcores.keySet()) {
      if (!requiredPackages.contains(pkg)) {
        warnings.add(
            new Warning(
                Warning.WarningType.UNUSED_METAMODEL,
                "Metamodel '" + pkg + "' not used",
                availableEcores.get(pkg)));
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