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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tools.vitruv.dsls.vitruvOCL.common.CompileError;
import tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.framework.vsum.VirtualModel;

/**
 * Main API for VitruvOCL constraint evaluation.
 *
 * <p>VitruvOCL implements semantics with unified dot-notation (no arrow operator), 1-based
 * indexing, and "everything is a collection" philosophy where single values become singletons and
 * null becomes empty collections.
 *
 * <p>Provides high-level methods for evaluating OCL constraints against Ecore models:
 *
 * <ul>
 *   <li>Single constraint evaluation with explicit file lists
 *   <li>Batch constraint evaluation from constraint files
 *   <li>Convention-over-configuration project evaluation
 *   <li>VSUM-integrated evaluation via {@link #registerVSUM(VirtualModel)}
 * </ul>
 *
 * <h2>VSUM Integration</h2>
 *
 * <p>When used within a Vitruvius project, constraints can be evaluated directly against a running
 * VSUM without providing explicit file paths. The VSUM already holds all model resources in memory,
 * so no additional file loading is needed.
 *
 * <pre>{@code
 * // Once at startup, register the VSUM:
 * VirtualModel vsum = new VirtualModelBuilder()...buildAndInitialize();
 * VitruvOCL.registerVSUM(vsum);
 *
 * // Then evaluate constraints without file paths:
 * BatchValidationResult results = VitruvOCL.evaluateConstraints(
 *     Path.of("constraints/templateConstraints.vitruvocl")
 * );
 *
 * // When disposing the VSUM:
 * vsum.dispose();
 * VitruvOCL.clearVSUM();
 * }</pre>
 *
 * <h2>Constraint Syntax</h2>
 *
 * VitruvOCL constraints must begin with {@code context} keyword:
 *
 * <pre>
 * context MetamodelName::ClassName inv:
 *   expression
 * </pre>
 *
 * <p><b>Key Syntax Differences from Standard OCL:</b>
 *
 * <ul>
 *   <li>Always use dot (.) for navigation - no arrow (->) operator
 *   <li>Use {@code !=} instead of {@code <>} for inequality
 *   <li>Fully qualified names: {@code spaceMission::Spacecraft}
 *   <li>All values are collections (singletons like {@code [5]} or empty {@code []})
 * </ul>
 *
 * <h2>Result Interpretation</h2>
 *
 * {@link ConstraintResult} provides multiple status indicators:
 *
 * <ul>
 *   <li>{@code isSuccess()} - No file loading or compilation errors occurred
 *   <li>{@code isSatisfied()} - Constraint evaluated to true for all instances
 *   <li>{@code getCompilerErrors()} - Syntax or type checking errors
 *   <li>{@code getFileErrors()} - Metamodel or instance loading failures
 *   <li>{@code getWarnings()} - Non-fatal issues (violations, duplicates, unused metamodels)
 * </ul>
 *
 * @see ConstraintResult for single constraint evaluation results
 * @see BatchValidationResult for multi-constraint validation results
 */
public class VitruvOCL {

  /**
   * The wrapper around the currently registered VSUM. {@code null} if no VSUM has been registered.
   *
   * <p>Set via {@link #registerVSUM(VirtualModel)}, cleared via {@link #clearVSUM()}.
   */
  private static VSUMWrapper vsumWrapper = null;

  // ---------------------------------------------------------------------------
  // VSUM registration
  // ---------------------------------------------------------------------------

  /**
   * Registers a VSUM as the model source for constraint evaluation.
   *
   * <p>Creates a {@link VSUMWrapper} that discovers and registers all metamodels from the VSUM's
   * in-memory resources. After calling this method, constraints can be evaluated without supplying
   * explicit file paths via {@link #evaluateConstraint(String)} and {@link
   * #evaluateConstraints(Path)}.
   *
   * <p>Replaces any previously registered VSUM. Call {@link #clearVSUM()} to deregister.
   *
   * @param vsum The VSUM to register. Must not be {@code null}.
   * @throws IllegalArgumentException if {@code vsum} is {@code null}
   */
  public static synchronized void registerVSUM(VirtualModel vsum) {
    if (vsum == null) {
      throw new IllegalArgumentException("VSUM must not be null");
    }
    vsumWrapper = new VSUMWrapper(vsum);
  }

  /**
   * Deregisters the currently registered VSUM and releases its resources.
   *
   * <p>Should be called when the VSUM is disposed to avoid holding stale references:
   *
   * <pre>{@code
   * vsum.dispose();
   * VitruvOCL.clearVSUM();
   * }</pre>
   */
  public static synchronized void clearVSUM() {
    vsumWrapper = null;
  }

  /**
   * Returns whether a VSUM is currently registered.
   *
   * @return {@code true} if a VSUM has been registered via {@link #registerVSUM}
   */
  public static synchronized boolean hasRegisteredVSUM() {
    return vsumWrapper != null;
  }

  // ---------------------------------------------------------------------------
  // VSUM-aware evaluation (no file paths needed)
  // ---------------------------------------------------------------------------

  /**
   * Evaluates a single constraint against the registered VSUM.
   *
   * @param constraint OCL constraint expression (must start with {@code context})
   * @return Evaluation result with satisfaction status and diagnostics
   * @throws IllegalStateException if no VSUM has been registered via {@link #registerVSUM}
   */
  public static ConstraintResult evaluateConstraint(String constraint) {
    return compileAndEvaluate(constraint, getVsumWrapper(), List.of());
  }

  /**
   * Evaluates all constraints from a file against the registered VSUM.
   *
   * @param constraintsFile File containing one or more constraint definitions
   * @return Batch validation result for all constraints in file
   * @throws IOException If the constraint file cannot be read
   * @throws IllegalStateException if no VSUM has been registered via {@link #registerVSUM}
   */
  public static BatchValidationResult evaluateConstraints(Path constraintsFile) throws IOException {
    List<String> constraints = parseConstraintsFile(constraintsFile);
    return evaluateConstraints(constraints, getVsumWrapper());
  }

  // ---------------------------------------------------------------------------
  // File-path-based evaluation (original API, unchanged)
  // ---------------------------------------------------------------------------

  /**
   * Evaluates single constraint against provided models.
   *
   * <p>Performs three-pass compilation pipeline:
   *
   * <ol>
   *   <li>Smart metamodel loading (only loads metamodels referenced in constraint)
   *   <li>Symbol table construction and type checking
   *   <li>Runtime evaluation against model instances
   * </ol>
   *
   * @param constraint OCL constraint expression (must start with {@code context})
   * @param ecoreFiles Metamodel definition files (.ecore)
   * @param xmiFiles Model instance files (any EMF-compatible extension)
   * @return Evaluation result with satisfaction status and diagnostics
   */
  public static ConstraintResult evaluateConstraint(
      String constraint, Path[] ecoreFiles, Path[] xmiFiles) {
    SmartLoader.LoadResult loadResult =
        SmartLoader.loadForConstraint(constraint, ecoreFiles, xmiFiles);

    if (loadResult.hasErrors()) {
      return new ConstraintResult(
          constraint, false, List.of(), loadResult.fileErrors, loadResult.warnings);
    }

    return compileAndEvaluate(constraint, loadResult.wrapper, loadResult.warnings);
  }

  public static BatchValidationResult evaluateConstraints(
      List<String> constraints, Path[] ecoreFiles, Path[] xmiFiles) {
    if (constraints.isEmpty()) {
      return new BatchValidationResult(List.of());
    }
    SmartLoader.LoadResult loadResult =
        SmartLoader.loadForConstraint(constraints.get(0), ecoreFiles, xmiFiles);

    if (loadResult.hasErrors()) {
      return new BatchValidationResult(
          constraints.stream()
              .map(
                  c ->
                      new ConstraintResult(
                          c, false, List.of(), loadResult.fileErrors, loadResult.warnings))
              .toList());
    }

    return evaluateConstraints(constraints, loadResult.wrapper);
  }

  /**
   * Evaluates constraints from file against provided models.
   *
   * @param constraintsFile File containing constraint definitions
   * @param ecoreFiles Metamodel definition files
   * @param xmiFiles Model instance files
   * @return Batch validation result for all constraints in file
   * @throws IOException If constraint file cannot be read
   */
  public static BatchValidationResult evaluateConstraints(
      Path constraintsFile, Path[] ecoreFiles, Path[] xmiFiles) throws IOException {
    List<String> constraints = parseConstraintsFile(constraintsFile);
    return evaluateConstraints(constraints, ecoreFiles, xmiFiles);
  }

  /**
   * Evaluates project using convention-over-configuration directory structure.
   *
   * <p>Expected structure:
   *
   * <pre>
   * projectDir/
   *   constraints.ocl       - Constraint definitions
   *   metamodels/           - All .ecore files
   *   instances/            - All model instance files
   * </pre>
   *
   * @param projectDir Root directory containing conventional structure
   * @return Batch validation result for all constraints
   * @throws IOException If files cannot be read or required directories don't exist
   */
  public static BatchValidationResult evaluateProject(Path projectDir) throws IOException {
    Path constraintsFile = projectDir.resolve("constraints.ocl");
    Path metamodelsDir = projectDir.resolve("metamodels");
    Path instancesDir = projectDir.resolve("instances");

    Path[] ecoreFiles = collectFiles(metamodelsDir, ".ecore");
    Path[] xmiFiles = collectFiles(instancesDir, ".xmi", ".spacemission", ".satellitesystem");

    return evaluateConstraints(constraintsFile, ecoreFiles, xmiFiles);
  }

  /**
   * Evaluates project with custom constraint file location.
   *
   * @param constraintsFile Path to constraints file
   * @param resourcesDir Directory containing metamodels/ and instances/ subdirectories
   * @return Batch validation result
   * @throws IOException If files cannot be read
   */
  public static BatchValidationResult evaluateProject(Path constraintsFile, Path resourcesDir)
      throws IOException {
    Path metamodelsDir = resourcesDir.resolve("metamodels");
    Path instancesDir = resourcesDir.resolve("instances");

    Path[] ecoreFiles = collectFiles(metamodelsDir, ".ecore");
    Path[] xmiFiles = collectFiles(instancesDir, ".xmi", ".spacemission", ".satellitesystem");

    return evaluateConstraints(constraintsFile, ecoreFiles, xmiFiles);
  }

  // ---------------------------------------------------------------------------
  // Shared compile + evaluate logic
  // ---------------------------------------------------------------------------

  /**
   * Runs the compiler pipeline and evaluates the constraint against the given wrapper.
   *
   * <p>Single entry point for both the file-path path (wrapper built by {@link SmartLoader}) and
   * the VSUM path (wrapper is a {@link VSUMWrapper}).
   *
   * @param constraint OCL constraint expression
   * @param wrapper Metamodel and instance access
   * @return Evaluation result
   */
  private static ConstraintResult compileAndEvaluate(
      String constraint, MetamodelWrapperInterface wrapper, List<Warning> loaderWarnings) {
    VitruvOCLCompiler compiler = new VitruvOCLCompiler(wrapper, null);
    Value result = compiler.compile(constraint);

    if (result == null) {
      return new ConstraintResult(
          constraint,
          false,
          List.of(
              new CompileError(
                  1, 0, "Syntax error in constraint", ErrorSeverity.ERROR, constraint)),
          List.of(),
          loaderWarnings);
    }

    List<CompileError> compilerErrors =
        compiler.hasErrors() ? compiler.getErrors().getErrors() : List.of();

    if (!compilerErrors.isEmpty()) {
      return new ConstraintResult(constraint, false, compilerErrors, List.of(), loaderWarnings);
    }

    boolean satisfied = true;
    List<Integer> violatingIndices = new ArrayList<>();

    for (int i = 0; i < result.size(); i++) {
      OCLElement elem = result.getElements().get(i);
      if (elem instanceof OCLElement.BoolValue boolVal) {
        if (!boolVal.value()) {
          satisfied = false;
          violatingIndices.add(i);
        }
      } else {
        satisfied = false;
        violatingIndices.add(i);
      }
    }

    List<Warning> warnings = new ArrayList<>(loaderWarnings);
    if (!violatingIndices.isEmpty()) {
      List<String> instanceNames = new ArrayList<>();
      for (int idx : violatingIndices) {
        String name = wrapper.getInstanceNameByIndex(idx);
        instanceNames.add(name != null ? name : "index_" + idx);
      }
      warnings.add(
          new Warning(
              Warning.WarningType.CONSTRAINT_VIOLATION,
              "Constraint violated for instances: " + instanceNames));
    }

    return new ConstraintResult(constraint, satisfied, compilerErrors, List.of(), warnings);
  }

  /** Evaluates multiple constraints against a wrapper, deduplicating duplicates. */
  private static BatchValidationResult evaluateConstraints(
      List<String> constraints, MetamodelWrapperInterface wrapper) {
    List<ConstraintResult> results = new ArrayList<>();
    Set<String> seenConstraints = new HashSet<>();

    for (String constraint : constraints) {
      if (seenConstraints.contains(constraint)) {
        results.add(duplicateResult(constraint));
        continue;
      }
      seenConstraints.add(constraint);
      results.add(compileAndEvaluate(constraint, wrapper, List.of()));
    }

    return new BatchValidationResult(results);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /** Returns the VSUM wrapper, throwing if no VSUM is registered. */
  private static synchronized VSUMWrapper getVsumWrapper() {
    if (vsumWrapper == null) {
      throw new IllegalStateException(
          "No VSUM registered. Call VitruvOCL.registerVSUM(vsum) before evaluating constraints "
              + "without explicit file paths.");
    }
    return vsumWrapper;
  }

  /** Creates a {@link ConstraintResult} representing a duplicate constraint warning. */
  private static ConstraintResult duplicateResult(String constraint) {
    return new ConstraintResult(
        constraint,
        false,
        List.of(),
        List.of(),
        List.of(
            new Warning(
                Warning.WarningType.DUPLICATE_CONSTRAINT, "Constraint specified multiple times")));
  }

  /**
   * Parses a constraint file into individual constraint strings.
   *
   * @param file Constraint file to parse
   * @return List of constraint expressions, each starting with {@code context}
   * @throws IOException If file cannot be read
   */
  private static List<String> parseConstraintsFile(Path file) throws IOException {
    String content = Files.readString(file);
    List<String> constraints = new ArrayList<>();

    String[] lines = content.split("\n");
    StringBuilder cleaned = new StringBuilder();
    for (String line : lines) {
      String trimmed = line.trim();
      if (!trimmed.startsWith("--") && !trimmed.isEmpty()) {
        cleaned.append(line).append("\n");
      }
    }

    String[] parts = cleaned.toString().split("(?=context\\s)");
    for (String part : parts) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty() && trimmed.startsWith("context")) {
        constraints.add(trimmed);
      }
    }

    return constraints;
  }

  /**
   * Recursively collects files with given extensions from directory.
   *
   * @param directory Directory to search recursively
   * @param extensions File extensions to match (e.g., ".ecore", ".xmi")
   * @return Array of matching file paths, empty if directory doesn't exist
   * @throws IOException If directory traversal fails
   */
  private static Path[] collectFiles(Path directory, String... extensions) throws IOException {
    if (!Files.exists(directory) || !Files.isDirectory(directory)) {
      return new Path[0];
    }

    try (Stream<Path> stream = Files.walk(directory)) {
      List<Path> files =
          stream
              .filter(Files::isRegularFile)
              .filter(
                  p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    for (String ext : extensions) {
                      if (name.endsWith(ext)) return true;
                    }
                    return false;
                  })
              .collect(Collectors.toList());

      return files.toArray(new Path[0]);
    }
  }
}