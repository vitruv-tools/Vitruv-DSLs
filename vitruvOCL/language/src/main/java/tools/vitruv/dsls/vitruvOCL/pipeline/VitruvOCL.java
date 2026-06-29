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
import java.util.stream.Stream;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.vitruvOCL.common.CompileError;
import tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.framework.vsum.VirtualModel;

public class VitruvOCL {

  private VitruvOCL() {}

  private static VSUMWrapper vsumWrapper = null;
  private static MetamodelWrapperInterface directWrapper = null;

  // ---------------------------------------------------------------------------
  // Registration
  // ---------------------------------------------------------------------------

  public static synchronized void registerVSUM(VirtualModel vsum) {
    if (vsum == null) {
      throw new IllegalArgumentException("VSUM must not be null");
    }
    vsumWrapper = new VSUMWrapper(vsum);
  }

  /**
   * Registers a direct {@link MetamodelWrapperInterface} for constraint evaluation.
   *
   * <p>Used by the language server when loading a VSUM directory directly via EMF without going
   * through {@link tools.vitruv.framework.vsum.VirtualModelBuilder}.
   *
   * @param wrapper the wrapper to register; must not be {@code null}
   */
  public static synchronized void registerDirectWrapper(MetamodelWrapperInterface wrapper) {
    if (wrapper == null) {
      throw new IllegalArgumentException("Wrapper must not be null");
    }
    directWrapper = wrapper;
  }

  public static synchronized void clearVSUM() {
    vsumWrapper = null;
    directWrapper = null;
  }

  public static synchronized boolean hasRegisteredVSUM() {
    return vsumWrapper != null || directWrapper != null;
  }

  // ---------------------------------------------------------------------------
  // VSUM-aware evaluation (no file paths needed)
  // ---------------------------------------------------------------------------

  public static ConstraintResult evaluateConstraint(String constraint) {
    return compileAndEvaluate(constraint, getVsumWrapper(), List.of());
  }

  /**
   * Evaluates all constraints in the given file against the registered VSUM.
   *
   * <p>If the constraints file cannot be read, a {@link BatchValidationResult} with a single error
   * entry is returned instead of throwing an exception. This allows callers to handle
   * file-not-found or I/O errors uniformly via the result object without checked exceptions.
   *
   * @param constraintsFile path to the {@code .ocl} constraints file
   * @return the validation result; never {@code null}
   */
  public static BatchValidationResult evaluateConstraints(Path constraintsFile) {
    List<String> constraints;
    try {
      constraints = parseConstraintsFile(constraintsFile);
    } catch (IOException e) {
      return new BatchValidationResult(
          List.of(
              new ConstraintResult(
                  constraintsFile.toString(),
                  false,
                  List.of(
                      new CompileError(
                          1,
                          0,
                          "Could not read constraints file: "
                              + constraintsFile.getFileName()
                              + " ("
                              + e.getMessage()
                              + ")",
                          ErrorSeverity.ERROR,
                          constraintsFile.toString())),
                  List.of(),
                  List.of())));
    }
    return evaluateConstraints(constraints, getVsumWrapper());
  }

  // ---------------------------------------------------------------------------
  // File-path-based evaluation
  // ---------------------------------------------------------------------------

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

  public static BatchValidationResult evaluateConstraints(
      Path constraintsFile, Path[] ecoreFiles, Path[] xmiFiles) throws IOException {
    List<String> constraints = parseConstraintsFile(constraintsFile);
    return evaluateConstraints(constraints, ecoreFiles, xmiFiles);
  }

  public static BatchValidationResult evaluateProject(Path projectDir) throws IOException {
    Path mainDir = projectDir.resolve("model/src/main");
    Path constraintsFile = mainDir.resolve("constraints.ocl");
    Path ecoreDir = mainDir.resolve("ecore");
    Path instancesDir = mainDir.resolve("instances");
    Path[] ecoreFiles = collectFiles(ecoreDir, ".ecore");
    Path[] xmiFiles = collectAllFiles(instancesDir);
    return evaluateConstraints(constraintsFile, ecoreFiles, xmiFiles);
  }

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
    List<EvaluationVisitor.ViolationRecord> records =
        evaluator != null ? evaluator.getViolationRecords() : List.of();

    boolean satisfied = records.isEmpty();
    String constraintName = extractConstraintName(constraint);
    for (EvaluationVisitor.ViolationRecord violation : records) {
      String sourceFile = wrapper.getSourceFileForInstance(violation.instance());
      String filename = sourceFile != null ? sourceFile : "unknown";
      String instanceLabel = describeInstance(violation.instance());
      String message = violation.customMessage() != null ? violation.customMessage() : instanceLabel;
      String block = formatViolationBlock(violation.severity(), constraintName, filename, instanceLabel, message);
      warnings.add(new Warning(Warning.WarningType.CONSTRAINT_VIOLATION, block));
    }

    return new ConstraintResult(constraint, satisfied, compilerErrors, List.of(), warnings);
  }

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

  private static synchronized MetamodelWrapperInterface getVsumWrapper() {
    if (directWrapper != null) {
      return directWrapper;
    }
    if (vsumWrapper != null) {
      return vsumWrapper;
    }
    throw new IllegalStateException(
        "No VSUM registered. Call VitruvOCL.registerVSUM(vsum) or "
            + "VitruvOCL.registerDirectWrapper(wrapper) before evaluating constraints "
            + "without explicit file paths.");
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
      if (feature.isMany()) continue;
      Object value = instance.eGet(feature);
      if (value instanceof String || value instanceof Integer || value instanceof Boolean) {
        parts.add(feature.getName() + "=\"" + value + "\"");
      }
      if (parts.size() >= 3) break;
    }
    sb.append(String.join(", ", parts)).append(")");
    return sb.toString();
  }

  private static final String VIOLATION_SEP = "-".repeat(57);

  private static String formatViolationBlock(
      String severity, String constraintName, String model, String object, String message) {
    return VIOLATION_SEP + "\n"
        + "[" + severity + "] " + constraintName + "\n"
        + "Model   : " + model + "\n"
        + "Object  : " + object + "\n"
        + "Message : " + message + "\n"
        + VIOLATION_SEP;
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
      // Skip comment lines and import declarations (e.g. "import model : '...'")
      if (!trimmed.startsWith("--") && !trimmed.startsWith("import ") && !trimmed.isEmpty()) {
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
    if (!Files.exists(directory) || !Files.isDirectory(directory)) return new Path[0];
    try (Stream<Path> stream = Files.walk(directory)) {
      return stream
          .filter(Files::isRegularFile)
          .filter(
              p -> {
                String name = p.getFileName().toString().toLowerCase();
                for (String ext : extensions) {
                  if (name.endsWith(ext)) return true;
                }
                return false;
              })
          .toList()
          .toArray(new Path[0]);
    }
  }

  private static Path[] collectAllFiles(Path directory) throws IOException {
    if (!Files.exists(directory) || !Files.isDirectory(directory)) return new Path[0];
    try (Stream<Path> stream = Files.walk(directory)) {
      return stream.filter(Files::isRegularFile).toList().toArray(new Path[0]);
    }
  }
}