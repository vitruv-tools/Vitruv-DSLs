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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.vitruvOCL.common.CompileError;
import tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
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
 * context MetamodelName::ClassName inv constraintName:
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
 * <h2>Violation Reporting</h2>
 *
 * <p>When a constraint is violated, one {@link Warning} of type {@code CONSTRAINT_VIOLATION} is
 * emitted per violating instance, in the format:
 *
 * <pre>
 * [VIOLATION] constraintName @ filename :: ClassName(attr1="val1", attr2="val2")
 * </pre>
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
  // File-path-based evaluation
  // ---------------------------------------------------------------------------

  /**
   * Evaluates single constraint against provided models.
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

  /**
   * Evaluates multiple constraints against provided models.
   *
   * @param constraints List of constraint expressions
   * @param ecoreFiles Metamodel definition files
   * @param xmiFiles Model instance files
   * @return Aggregated batch validation result
   */
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
   *   model/src/main/
   *     constraints.ocl    - Constraint definitions
   *     ecore/             - All .ecore files
   *     instances/         - All model instance files
   * </pre>
   *
   * @param projectDir Root directory of the project
   * @return Batch validation result for all constraints
   * @throws IOException If files cannot be read or required directories don't exist
   */
  public static BatchValidationResult evaluateProject(Path projectDir) throws IOException {
    Path mainDir = projectDir.resolve("model/src/main");
    Path constraintsFile = mainDir.resolve("constraints.ocl");
    Path ecoreDir = mainDir.resolve("ecore");
    Path instancesDir = mainDir.resolve("instances");

    Path[] ecoreFiles = collectFiles(ecoreDir, ".ecore");
    Path[] xmiFiles = collectAllFiles(instancesDir);

    return evaluateConstraints(constraintsFile, ecoreFiles, xmiFiles);
  }

  /**
   * Evaluates project with custom constraint file location.
   *
   * @param constraintsFile Path to constraints file
   * @param resourcesDir Root directory containing model/src/main/ecore and model/src/main/instances
   * @return Batch validation result
   * @throws IOException If files cannot be read
   */
  public static BatchValidationResult evaluateProject(Path constraintsFile, Path resourcesDir)
      throws IOException {
    Path mainDir = resourcesDir.resolve("model/src/main");
    Path ecoreDir = mainDir.resolve("ecore");
    Path instancesDir = mainDir.resolve("instances");

    Path[] ecoreFiles = collectFiles(ecoreDir, ".ecore");
    Path[] xmiFiles = collectAllFiles(instancesDir);

    return evaluateConstraints(constraintsFile, ecoreFiles, xmiFiles);
  }

  // ---------------------------------------------------------------------------
  // Shared compile + evaluate logic
  // ---------------------------------------------------------------------------

  private static ConstraintResult compileAndEvaluate(
      String constraint, MetamodelWrapperInterface wrapper, List<Warning> loaderWarnings) {
    VitruvOCLCompiler compiler = new VitruvOCLCompiler(wrapper, null);
    Value result = compiler.compile(constraint);

    if (result == null) {
      List<CompileError> passErrors =
          compiler.hasErrors() ? compiler.getErrors().getErrors() : List.of();

      if (passErrors.isEmpty()) {
        passErrors =
            List.of(
                new CompileError(
                    1, 0, "Syntax error in constraint", ErrorSeverity.ERROR, constraint));
      }
      return new ConstraintResult(constraint, false, passErrors, List.of(), loaderWarnings);
    }

    List<CompileError> compilerErrors =
        compiler.hasErrors() ? compiler.getErrors().getErrors() : List.of();

    if (!compilerErrors.isEmpty()) {
      return new ConstraintResult(constraint, false, compilerErrors, List.of(), loaderWarnings);
    }

    List<Warning> warnings = new ArrayList<>(loaderWarnings);

    EvaluationVisitor evaluator = compiler.getLastEvaluator();
    List<EObject> violatingInstances =
        evaluator != null ? evaluator.getViolatingInstances() : List.of();

    boolean satisfied = violatingInstances.isEmpty();

    for (EObject instance : violatingInstances) {
      String sourceFile = wrapper.getSourceFileForInstance(instance);
      String filename = sourceFile != null ? sourceFile : "unknown";
      String instanceLabel = describeInstance(instance);
      String constraintName = extractConstraintName(constraint);
      warnings.add(
          new Warning(
              Warning.WarningType.CONSTRAINT_VIOLATION,
              "[VIOLATION] " + constraintName + " @ " + filename + " :: " + instanceLabel));
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

  private static synchronized VSUMWrapper getVsumWrapper() {
    if (vsumWrapper == null) {
      throw new IllegalStateException(
          "No VSUM registered. Call VitruvOCL.registerVSUM(vsum) before evaluating constraints "
              + "without explicit file paths.");
    }
    return vsumWrapper;
  }

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

  private static String describeInstance(EObject instance) {
    StringBuilder sb = new StringBuilder(instance.eClass().getName()).append("(");
    List<String> parts = new ArrayList<>();
    for (EStructuralFeature feature : instance.eClass().getEAllStructuralFeatures()) {
      if (feature.isMany()) {
        continue;
      }
      Object value = instance.eGet(feature);
      if (value instanceof String || value instanceof Integer || value instanceof Boolean) {
        parts.add(feature.getName() + "=\"" + value + "\"");
      }
      if (parts.size() >= 3) {
        break;
      }
    }
    sb.append(String.join(", ", parts)).append(")");
    return sb.toString();
  }

  private static String extractConstraintName(String constraint) {
    java.util.regex.Matcher m =
        java.util.regex.Pattern.compile("inv\\s+(\\w+)\\s*:").matcher(constraint);
    return m.find() ? m.group(1) : "unnamed";
  }

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
                      if (name.endsWith(ext)) {
                        return true;
                      }
                    }
                    return false;
                  })
              .collect(Collectors.toList());

      return files.toArray(new Path[0]);
    }
  }

  private static Path[] collectAllFiles(Path directory) throws IOException {
    if (!Files.exists(directory) || !Files.isDirectory(directory)) {
      return new Path[0];
    }

    try (Stream<Path> stream = Files.walk(directory)) {
      return stream.filter(Files::isRegularFile).collect(Collectors.toList()).toArray(new Path[0]);
    }
  }
}