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
 * <p>Provides high-level methods for evaluating OCL constraints against EMF models:
 *
 * <ul>
 *   <li>Single constraint evaluation with explicit file lists
 *   <li>Batch constraint evaluation from constraint files
 *   <li>Convention-over-configuration project evaluation
 * </ul>
 *
 * <p>Project structure convention:
 *
 * <pre>
 * project/
 *   constraints.ocl       - Constraint definitions
 *   metamodels/           - .ecore files
 *   instances/            - .xmi/.spacemission/.satellitesystem files
 * </pre>
 */
public class VitruvOCL {

  /**
   * Evaluates single constraint against provided models.
   *
   * <p>Performs smart loading (only required metamodels), full compilation pipeline (3-pass), and
   * satisfaction checking. Returns comprehensive result with errors, warnings, and violation
   * details.
   *
   * @param constraint OCL constraint expression
   * @param ecoreFiles Metamodel files
   * @param xmiFiles Model instance files
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
      warnings.add(
          new Warning(
              Warning.WarningType.CONSTRAINT_VIOLATION,
              "Constraint violated for instances at indices: " + violatingIndices));
    }

    return new ConstraintResult(
        constraint, satisfied, compilerErrors, loadResult.fileErrors, warnings);
  }

  /**
   * Evaluates multiple constraints, deduplicating and reporting duplicates as warnings.
   *
   * @param constraints List of constraint expressions
   * @param ecoreFiles Metamodel files
   * @param xmiFiles Model instance files
   * @return Aggregated batch validation result
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
   * @param constraintsFile File containing constraint definitions
   * @param ecoreFiles Metamodel files
   * @param xmiFiles Model instance files
   * @return Batch validation result
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
   *   constraints.ocl
   *   metamodels/
   *   instances/
   * </pre>
   *
   * @param projectDir Root directory containing conventional structure
   * @return Batch validation result
   * @throws IOException If files cannot be read or directories don't exist
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

  /**
   * Parses constraint file, extracting individual constraint definitions.
   *
   * <p>Removes comment lines (starting with --) and splits by "context" keyword.
   *
   * @param file Constraint file to parse
   * @return List of constraint expressions
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
   * @param directory Directory to search
   * @param extensions File extensions to match (e.g., ".ecore", ".xmi")
   * @return Array of matching file paths (empty if directory doesn't exist)
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