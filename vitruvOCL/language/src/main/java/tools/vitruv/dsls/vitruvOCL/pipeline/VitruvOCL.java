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
 * </ul>
 *
 * <p><b>Thread Safety:</b> All methods are thread-safe. Each invocation creates isolated compiler
 * and loader instances with no shared mutable state.
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
 * <h2>Usage Examples</h2>
 *
 * <h3>Single Constraint Evaluation</h3>
 *
 * <pre>{@code
 * // Within-metamodel constraint
 * ConstraintResult result = VitruvOCL.evaluateConstraint(
 *     "context spaceMission::Spacecraft inv: self.mass > 0",
 *     new Path[]{Path.of("spacemission.ecore")},
 *     new Path[]{Path.of("voyager.spacemission")}
 * );
 *
 * if (result.isSuccess() && result.isSatisfied()) {
 *     System.out.println("Constraint satisfied!");
 * } else {
 *     result.getCompilerErrors().forEach(System.err::println);
 *     result.getWarnings().forEach(System.err::println);
 * }
 * }</pre>
 *
 * <h3>Cross-Metamodel Constraint</h3>
 *
 * <pre>{@code
 * // Reference entities from different metamodels
 * String constraint = """
 *     context spaceMission::Spacecraft inv:
 *       satelliteSystem::Satellite.allInstances().collect(sat |
 *         sat.massKg
 *       ).sum() > self.mass
 *     """;
 *
 * ConstraintResult result = VitruvOCL.evaluateConstraint(
 *     constraint,
 *     new Path[]{
 *         Path.of("spacemission.ecore"),
 *         Path.of("satellitesystem.ecore")
 *     },
 *     new Path[]{
 *         Path.of("voyager.spacemission"),
 *         Path.of("atlas.satellitesystem")
 *     }
 * );
 * }</pre>
 *
 * <h3>Batch Evaluation from File</h3>
 *
 * <pre>{@code
 * // constraints.ocl contains multiple constraints
 * BatchValidationResult results = VitruvOCL.evaluateConstraints(
 *     Path.of("constraints.ocl"),
 *     new Path[]{Path.of("model.ecore")},
 *     new Path[]{Path.of("instance.xmi")}
 * );
 *
 * System.out.println("Satisfied: " + results.getSatisfiedCount());
 * System.out.println("Violated: " + results.getViolatedCount());
 * }</pre>
 *
 * <h3>Project-Based Evaluation (Convention over Configuration)</h3>
 *
 * <pre>
 * project/
 *   constraints.ocl              - Constraint definitions
 *   metamodels/
 *     spacemission.ecore
 *     satellitesystem.ecore
 *   instances/
 *     voyager.spacemission
 *     atlas.satellitesystem
 * </pre>
 *
 * <pre>{@code
 * BatchValidationResult results = VitruvOCL.evaluateProject(
 *     Path.of("project")
 * );
 * }</pre>
 *
 * <h2>Constraint File Format</h2>
 *
 * Multiple constraints can be defined in a single file:
 *
 * <pre>
 * -- Comments start with double dash
 * context spaceMission::Spacecraft inv:
 *   self.mass > 0
 *
 * context satelliteSystem::Satellite inv:
 *   self.serialNumber.size() > 0
 * </pre>
 *
 * <p>Each constraint must begin with {@code context} keyword. Lines starting with {@code --} are
 * treated as comments and ignored.
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
 * <p><b>Important:</b> {@code isSatisfied() == false} indicates constraint violation (instances
 * exist where the constraint evaluates to false), not compilation errors. Check {@code isSuccess()}
 * first to ensure the constraint was successfully evaluated.
 *
 * <h2>File Extensions</h2>
 *
 * Model instance files use extensions matching their metamodel name:
 *
 * <ul>
 *   <li>{@code .ecore} - Metamodel definitions
 *   <li>{@code .xmi} - Generic EMF instances
 *   <li>{@code .spacemission} - Instances of spaceMission metamodel
 *   <li>{@code .satellitesystem} - Instances of satelliteSystem metamodel
 * </ul>
 *
 * <p>The extension after the dot corresponds to the metamodel package name (e.g., {@code
 * spaceMission.ecore} creates instances with {@code .spacemission} extension).
 *
 * @see ConstraintResult for single constraint evaluation results
 * @see BatchValidationResult for multi-constraint validation results
 */
public class VitruvOCL {

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
   * <p>The constraint is evaluated for each instance of the context type. Result indicates whether
   * ALL instances satisfy the constraint.
   *
   * @param constraint OCL constraint expression (must start with {@code context})
   * @param ecoreFiles Metamodel definition files (.ecore)
   * @param xmiFiles Model instance files (any EMF-compatible extension)
   * @return Evaluation result with satisfaction status and diagnostics. Use {@code isSuccess()} to
   *     check for compilation errors, {@code isSatisfied()} to check constraint satisfaction.
   */
  public static ConstraintResult evaluateConstraint(
      String constraint, Path[] ecoreFiles, Path[] xmiFiles) {
    SmartLoader.LoadResult loadResult =
        SmartLoader.loadForConstraint(constraint, ecoreFiles, xmiFiles);

    if (loadResult.hasErrors()) {
      return new ConstraintResult(
          constraint, false, List.of(), loadResult.fileErrors, loadResult.warnings);
    }

    VitruvOCLCompiler compiler = new VitruvOCLCompiler(loadResult.wrapper, null);
    Value result = compiler.compile(constraint);

    if (result == null) {
      return new ConstraintResult(
          constraint,
          false,
          List.of(
              new CompileError(
                  1, 0, "Syntax error in constraint", ErrorSeverity.ERROR, constraint)),
          loadResult.fileErrors,
          loadResult.warnings);
    }

    List<CompileError> compilerErrors =
        compiler.hasErrors() ? compiler.getErrors().getErrors() : List.of();

    if (!compilerErrors.isEmpty()) {
      return new ConstraintResult(
          constraint, false, compilerErrors, loadResult.fileErrors, loadResult.warnings);
    }

    boolean satisfied = true;
    List<Integer> violatingIndices = new ArrayList<>();
    List<Warning> warnings = new ArrayList<>(loadResult.warnings);

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

    if (!violatingIndices.isEmpty()) {
      // Hole Instanznamen aus dem Wrapper
      List<String> instanceNames = new ArrayList<>();
      for (int idx : violatingIndices) {
        String name = loadResult.wrapper.getInstanceNameByIndex(idx); // NEU
        instanceNames.add(name != null ? name : "index_" + idx);
      }

      warnings.add(
          new Warning(
              Warning.WarningType.CONSTRAINT_VIOLATION,
              "Constraint violated for instances: " + instanceNames));
    }

    return new ConstraintResult(
        constraint, satisfied, compilerErrors, loadResult.fileErrors, warnings);
  }

  /**
   * Evaluates multiple constraints, deduplicating and reporting duplicates as warnings.
   *
   * <p>Each constraint is evaluated independently. Duplicate constraints (exact string match) are
   * detected and marked with warnings but not re-evaluated.
   *
   * @param constraints List of constraint expressions (each must start with {@code context})
   * @param ecoreFiles Metamodel definition files
   * @param xmiFiles Model instance files
   * @return Aggregated batch validation result containing individual constraint results
   */
  public static BatchValidationResult evaluateConstraints(
      List<String> constraints, Path[] ecoreFiles, Path[] xmiFiles) {
    List<ConstraintResult> results = new ArrayList<>();
    Set<String> seenConstraints = new HashSet<>();

    for (String constraint : constraints) {
      if (seenConstraints.contains(constraint)) {
        ConstraintResult duplicate =
            new ConstraintResult(
                constraint,
                false,
                List.of(),
                List.of(),
                List.of(
                    new Warning(
                        Warning.WarningType.DUPLICATE_CONSTRAINT,
                        "Constraint specified multiple times")));
        results.add(duplicate);
        continue;
      }

      seenConstraints.add(constraint);
      results.add(evaluateConstraint(constraint, ecoreFiles, xmiFiles));
    }

    return new BatchValidationResult(results);
  }

  /**
   * Evaluates constraints from file.
   *
   * <p>Parses constraint file format:
   *
   * <pre>
   * -- Comments start with double dash
   * context Type1 inv:
   *   expression1
   *
   * context Type2 inv:
   *   expression2
   * </pre>
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
   * <p>Automatically discovers all metamodels and instances in respective directories. Instance
   * files can have any EMF-compatible extension (.xmi, .spacemission, .satellitesystem, etc.).
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
   * <p>Uses convention-over-configuration for metamodels and instances, but allows constraints file
   * to be located anywhere.
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

  /**
   * Parses constraint file, extracting individual constraint definitions.
   *
   * <p>Splits file by {@code context} keyword after removing comment lines (starting with {@code
   * --}).
   *
   * @param file Constraint file to parse
   * @return List of constraint expressions, each starting with {@code context}
   * @throws IOException If file cannot be read
   */
  private static List<String> parseConstraintsFile(Path file) throws IOException {
    String content = Files.readString(file);
    List<String> constraints = new ArrayList<>();

    // Remove comments
    String[] lines = content.split("\n");
    StringBuilder cleaned = new StringBuilder();
    for (String line : lines) {
      String trimmed = line.trim();
      if (!trimmed.startsWith("--") && !trimmed.isEmpty()) {
        cleaned.append(line).append("\n");
      }
    }

    // Split by "context" keyword
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
   * <p>Extensions are case-insensitive. Returns empty array if directory doesn't exist (allowing
   * graceful handling of optional directories).
   *
   * @param directory Directory to search recursively
   * @param extensions File extensions to match (e.g., ".ecore", ".xmi", ".spacemission")
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
}