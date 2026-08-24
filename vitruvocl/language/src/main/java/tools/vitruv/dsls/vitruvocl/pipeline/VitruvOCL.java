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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.vitruvocl.common.CompileError;
import tools.vitruv.dsls.vitruvocl.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;
import tools.vitruv.framework.vsum.VirtualModel;

/** Static facade for evaluating VitruvOCL constraints against a VSUM or standalone files. */
public class VitruvOCL {

  private VitruvOCL() {}

  private static VsumWrapper vsumWrapper = null;
  private static MetamodelWrapperInterface directWrapper = null;

  /**
   * Default worker-thread count for {@link ConstraintListEvaluator}-backed batch evaluation, used
   * whenever a caller does not pass an explicit thread pool size.
   *
   * <p>Every batch-evaluation entry point also accepts an explicit {@code threadPoolSize}
   * parameter so callers can sweep thread counts for scaling benchmarks.
   */
  private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

  // ---------------------------------------------------------------------------
  // Registration
  // ---------------------------------------------------------------------------

  /**
   * Registers the VSUM to evaluate constraints against.
   *
   * @param vsum the virtual model; must not be {@code null}
   */
  public static synchronized void registerVSUM(VirtualModel vsum) {
    if (vsum == null) {
      throw new IllegalArgumentException("VSUM must not be null");
    }
    vsumWrapper = new VsumWrapper(vsum);
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

  /** Clears any registered VSUM or direct wrapper. */
  public static synchronized void clearVSUM() {
    vsumWrapper = null;
    directWrapper = null;
  }

  /**
   * Returns whether a VSUM or direct wrapper is currently registered.
   *
   * @return {@code true} if a VSUM or direct wrapper is registered
   */
  public static synchronized boolean hasRegisteredVSUM() {
    return vsumWrapper != null || directWrapper != null;
  }

  // ---------------------------------------------------------------------------
  // Evaluation (VSUM-aware and file-path-based; overloads kept together per checkstyle)
  // ---------------------------------------------------------------------------

  /**
   * Evaluates a single constraint against the registered VSUM.
   *
   * @param constraint the OCL constraint expression
   * @return the evaluation result
   */
  public static ConstraintResult evaluateConstraint(String constraint) {
    return compileAndEvaluate(constraint, getVsumWrapper(), List.of(), List.of(), false);
  }

  /**
   * Evaluates a single constraint against the registered VSUM, with an explicit transaction.
   *
   * @param constraint the OCL constraint expression
   * @param transaction the ordered list of atomic changes between pre-state and the current
   *     post-state, evaluated by {@code @pre}/{@code OCLisNew}/{@code OCLisModified}/{@code
   *     OCLisDeleted} (may be empty)
   * @return the evaluation result
   */
  public static ConstraintResult evaluateConstraint(
      String constraint, List<EChange<EObject>> transaction) {
    return compileAndEvaluate(constraint, getVsumWrapper(), List.of(), transaction, true);
  }

  /**
   * Evaluates a single constraint against metamodels and instances loaded from files.
   *
   * @param constraint the OCL constraint expression
   * @param ecoreFiles metamodel files to load
   * @param xmiFiles model instance files to load
   * @return the evaluation result
   */
  public static ConstraintResult evaluateConstraint(
      String constraint, Path[] ecoreFiles, Path[] xmiFiles) {
    return evaluateConstraintFromFiles(constraint, ecoreFiles, xmiFiles, List.of(), false);
  }

  /**
   * Evaluates a single constraint against metamodels and instances loaded from files, with an
   * explicit transaction.
   *
   * @param constraint the OCL constraint expression
   * @param ecoreFiles metamodel files to load
   * @param xmiFiles model instance files to load
   * @param transaction the ordered list of atomic changes between pre-state and the current
   *     post-state, evaluated by {@code @pre}/{@code OCLisNew}/{@code OCLisModified}/{@code
   *     OCLisDeleted} (may be empty)
   * @return the evaluation result
   */
  public static ConstraintResult evaluateConstraint(
      String constraint, Path[] ecoreFiles, Path[] xmiFiles, List<EChange<EObject>> transaction) {
    return evaluateConstraintFromFiles(constraint, ecoreFiles, xmiFiles, transaction, true);
  }

  private static ConstraintResult evaluateConstraintFromFiles(
      String constraint,
      Path[] ecoreFiles,
      Path[] xmiFiles,
      List<EChange<EObject>> transaction,
      boolean transactionSupported) {
    SmartLoader.LoadResult loadResult =
        SmartLoader.loadForConstraint(constraint, ecoreFiles, xmiFiles);
    if (loadResult.hasErrors()) {
      return new ConstraintResult(
          constraint, false, List.of(), loadResult.fileErrors, loadResult.warnings);
    }
    return compileAndEvaluate(
        constraint, loadResult.wrapper, loadResult.warnings, transaction, transactionSupported);
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
    return evaluateConstraints(constraintsFile, DEFAULT_THREAD_POOL_SIZE);
  }

  /**
   * Evaluates all constraints in the given file against the registered VSUM, distributing the
   * evaluation across {@code threadPoolSize} worker threads.
   *
   * @param constraintsFile path to the {@code .ocl} constraints file
   * @param threadPoolSize number of worker threads used to evaluate the constraint list; must be
   *     at least 1
   * @return the validation result; never {@code null}
   */
  public static BatchValidationResult evaluateConstraints(Path constraintsFile, int threadPoolSize) {
    return evaluateConstraintsFromPath(constraintsFile, threadPoolSize, List.of(), false);
  }

  /**
   * Evaluates all constraints in the given file against the registered VSUM, with an explicit
   * transaction shared by every constraint in the file.
   *
   * <p>Unlike {@link #evaluateConstraints(Path)}, {@code pre}/{@code post} blocks are genuinely
   * evaluated here (not skipped) — this is the entry point for callers that have a real
   * transaction available, such as a Reaction-execution hook checking the constraints relevant to
   * whichever Reactions just fired. See {@link #evaluateConstraint(String, List)} for the
   * single-constraint equivalent and the meaning of {@code transaction}.
   *
   * @param constraintsFile path to the {@code .ocl} constraints file
   * @param transaction the ordered list of atomic changes between pre-state and the current
   *     post-state, evaluated by {@code @pre}/{@code OCLisNew}/{@code OCLisModified}/{@code
   *     OCLisDeleted} (may be empty, but must not be {@code null} — pass {@link
   *     #evaluateConstraints(Path)} instead if there genuinely is no transaction context)
   * @return the validation result; never {@code null}
   */
  public static BatchValidationResult evaluateConstraints(
      Path constraintsFile, List<EChange<EObject>> transaction) {
    return evaluateConstraintsFromPath(
        constraintsFile, DEFAULT_THREAD_POOL_SIZE, transaction, true);
  }

  private static BatchValidationResult evaluateConstraintsFromPath(
      Path constraintsFile,
      int threadPoolSize,
      List<EChange<EObject>> transaction,
      boolean transactionSupported) {
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
    return evaluateConstraints(
        constraints, getVsumWrapper(), threadPoolSize, transaction, transactionSupported);
  }

  /**
   * Evaluates multiple constraints against metamodels and instances loaded from files.
   *
   * @param constraints the OCL constraint expressions
   * @param ecoreFiles metamodel files to load
   * @param xmiFiles model instance files to load
   * @return the batch evaluation result
   */
  public static BatchValidationResult evaluateConstraints(
      List<String> constraints, Path[] ecoreFiles, Path[] xmiFiles) {
    return evaluateConstraints(constraints, ecoreFiles, xmiFiles, DEFAULT_THREAD_POOL_SIZE);
  }

  /**
   * Evaluates multiple constraints against metamodels and instances loaded from files,
   * distributing the evaluation across {@code threadPoolSize} worker threads.
   *
   * @param constraints the OCL constraint expressions
   * @param ecoreFiles metamodel files to load
   * @param xmiFiles model instance files to load
   * @param threadPoolSize number of worker threads used to evaluate the constraint list; must be
   *     at least 1
   * @return the batch evaluation result
   */
  public static BatchValidationResult evaluateConstraints(
      List<String> constraints, Path[] ecoreFiles, Path[] xmiFiles, int threadPoolSize) {
    if (constraints.isEmpty()) {
      return new BatchValidationResult(List.of());
    }
    // Union of every constraint's dependencies (not just the first), so constraints referencing
    // different metamodel packages within the same batch all resolve correctly — see
    // SmartLoader#loadForConstraints. This runs entirely before any parallel task is submitted.
    SmartLoader.LoadResult loadResult =
        SmartLoader.loadForConstraints(constraints, ecoreFiles, xmiFiles);
    if (loadResult.hasErrors()) {
      return new BatchValidationResult(
          constraints.stream()
              .map(
                  c ->
                      new ConstraintResult(
                          c, false, List.of(), loadResult.fileErrors, loadResult.warnings))
              .toList());
    }
    return evaluateConstraints(constraints, loadResult.wrapper, threadPoolSize);
  }

  /**
   * Evaluates constraints read from a file against metamodels and instances loaded from files.
   *
   * @param constraintsFile path to the {@code .ocl} constraints file
   * @param ecoreFiles metamodel files to load
   * @param xmiFiles model instance files to load
   * @return the batch evaluation result
   * @throws IOException if the constraints file cannot be read
   */
  public static BatchValidationResult evaluateConstraints(
      Path constraintsFile, Path[] ecoreFiles, Path[] xmiFiles) throws IOException {
    return evaluateConstraints(constraintsFile, ecoreFiles, xmiFiles, DEFAULT_THREAD_POOL_SIZE);
  }

  /**
   * Evaluates constraints read from a file against metamodels and instances loaded from files,
   * distributing the evaluation across {@code threadPoolSize} worker threads.
   *
   * @param constraintsFile path to the {@code .ocl} constraints file
   * @param ecoreFiles metamodel files to load
   * @param xmiFiles model instance files to load
   * @param threadPoolSize number of worker threads used to evaluate the constraint list; must be
   *     at least 1
   * @return the batch evaluation result
   * @throws IOException if the constraints file cannot be read
   */
  public static BatchValidationResult evaluateConstraints(
      Path constraintsFile, Path[] ecoreFiles, Path[] xmiFiles, int threadPoolSize)
      throws IOException {
    List<String> constraints = parseConstraintsFile(constraintsFile);
    return evaluateConstraints(constraints, ecoreFiles, xmiFiles, threadPoolSize);
  }

  /**
   * Evaluates {@code constraints} against {@code wrapper}, distributing the work across {@code
   * threadPoolSize} worker threads via {@link ConstraintListEvaluator}.
   *
   * <p>Duplicate detection runs as a cheap, sequential pre-pass over the constraint strings
   * themselves (no compilation involved) so that only the first occurrence of a given constraint
   * text is ever submitted for (possibly parallel) compilation and evaluation; later duplicates are
   * resolved directly to a {@link #duplicateResult}. This keeps duplicate-detection order-stable
   * regardless of thread pool size, and avoids redundant work. Each submitted constraint is
   * compiled and evaluated by {@link #compileAndEvaluate}, which creates its own {@link
   * VitruvOCLCompiler} (and thus its own {@link tools.vitruv.dsls.vitruvocl.common.ErrorCollector})
   * per call — so no compiler-level state is shared between concurrently-running tasks. Results are
   * returned in the same order as {@code constraints}, independent of completion order.
   *
   * @param constraints the OCL constraint expressions, in the order results should be returned
   * @param wrapper shared metamodel/instance access, reused (read-mostly) across all tasks
   * @param threadPoolSize number of worker threads used to evaluate the constraint list; must be
   *     at least 1
   * @return the batch evaluation result, in input order
   */
  private static BatchValidationResult evaluateConstraints(
      List<String> constraints, MetamodelWrapperInterface wrapper, int threadPoolSize) {
    return evaluateConstraints(constraints, wrapper, threadPoolSize, List.of(), false);
  }

  /**
   * Same as {@link #evaluateConstraints(List, MetamodelWrapperInterface, int)}, but with an
   * explicit transaction shared by every constraint in {@code constraints} — the transaction is
   * immutable and read-only, so sharing it read-only across the (possibly parallel) evaluation
   * tasks below is as safe as sharing {@code wrapper} already is.
   *
   * @param transaction the ordered list of atomic changes between pre-state and the current
   *     post-state, evaluated by {@code @pre}/{@code OCLisNew}/{@code OCLisModified}/{@code
   *     OCLisDeleted} (may be empty)
   * @param transactionSupported whether {@code pre}/{@code post} blocks should be genuinely
   *     evaluated against {@code transaction} ({@code true}) or skipped outright because this call
   *     site has no transaction context at all ({@code false}) — see {@link
   *     tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor}'s field of the same name
   */
  private static BatchValidationResult evaluateConstraints(
      List<String> constraints,
      MetamodelWrapperInterface wrapper,
      int threadPoolSize,
      List<EChange<EObject>> transaction,
      boolean transactionSupported) {
    int size = constraints.size();
    List<ConstraintResult> results = new ArrayList<>(Collections.nCopies(size, null));
    Set<String> seenConstraints = new HashSet<>();
    List<String> toEvaluate = new ArrayList<>();
    List<Integer> toEvaluateIndices = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      String constraint = constraints.get(i);
      if (!seenConstraints.add(constraint)) {
        results.set(i, duplicateResult(constraint));
      } else {
        toEvaluate.add(constraint);
        toEvaluateIndices.add(i);
      }
    }

    List<ConstraintResult> evaluated =
        ConstraintListEvaluator.evaluate(
            toEvaluate,
            c -> compileAndEvaluate(c, wrapper, List.of(), transaction, transactionSupported),
            threadPoolSize);

    for (int i = 0; i < evaluated.size(); i++) {
      results.set(toEvaluateIndices.get(i), evaluated.get(i));
    }

    return new BatchValidationResult(results);
  }

  /**
   * Evaluates a project's constraints against its default metamodel and instance directories.
   *
   * @param projectDir project root; expects {@code model/src/main/{constraints.ocl,ecore,
   *     instances}}
   * @return the batch evaluation result
   * @throws IOException if the constraints file cannot be read
   */
  public static BatchValidationResult evaluateProject(Path projectDir) throws IOException {
    return evaluateProject(projectDir, DEFAULT_THREAD_POOL_SIZE);
  }

  /**
   * Evaluates a project's constraints against its default metamodel and instance directories,
   * distributing the evaluation across {@code threadPoolSize} worker threads.
   *
   * @param projectDir project root; expects {@code model/src/main/{constraints.ocl,ecore,
   *     instances}}
   * @param threadPoolSize number of worker threads used to evaluate the constraint list; must be
   *     at least 1
   * @return the batch evaluation result
   * @throws IOException if the constraints file cannot be read
   */
  public static BatchValidationResult evaluateProject(Path projectDir, int threadPoolSize)
      throws IOException {
    Path mainDir = projectDir.resolve("model/src/main");
    Path constraintsFile = mainDir.resolve("constraints.ocl");
    Path ecoreDir = mainDir.resolve("ecore");
    Path instancesDir = mainDir.resolve("instances");
    Path[] ecoreFiles = collectFiles(ecoreDir, ".ecore");
    Path[] xmiFiles = collectAllFiles(instancesDir);
    return evaluateConstraints(constraintsFile, ecoreFiles, xmiFiles, threadPoolSize);
  }

  /**
   * Evaluates constraints from an explicit file against a project's default metamodel and instance
   * directories.
   *
   * @param constraintsFile path to the {@code .ocl} constraints file
   * @param resourcesDir project root; expects {@code model/src/main/{ecore,instances}}
   * @return the batch evaluation result
   * @throws IOException if the constraints file cannot be read
   */
  public static BatchValidationResult evaluateProject(Path constraintsFile, Path resourcesDir)
      throws IOException {
    return evaluateProject(constraintsFile, resourcesDir, DEFAULT_THREAD_POOL_SIZE);
  }

  /**
   * Evaluates constraints from an explicit file against a project's default metamodel and instance
   * directories, distributing the evaluation across {@code threadPoolSize} worker threads.
   *
   * @param constraintsFile path to the {@code .ocl} constraints file
   * @param resourcesDir project root; expects {@code model/src/main/{ecore,instances}}
   * @param threadPoolSize number of worker threads used to evaluate the constraint list; must be
   *     at least 1
   * @return the batch evaluation result
   * @throws IOException if the constraints file cannot be read
   */
  public static BatchValidationResult evaluateProject(
      Path constraintsFile, Path resourcesDir, int threadPoolSize) throws IOException {
    Path mainDir = resourcesDir.resolve("model/src/main");
    Path ecoreDir = mainDir.resolve("ecore");
    Path instancesDir = mainDir.resolve("instances");
    Path[] ecoreFiles = collectFiles(ecoreDir, ".ecore");
    Path[] xmiFiles = collectAllFiles(instancesDir);
    return evaluateConstraints(constraintsFile, ecoreFiles, xmiFiles, threadPoolSize);
  }

  // ---------------------------------------------------------------------------
  // Shared compile + evaluate logic
  // ---------------------------------------------------------------------------

  private static ConstraintResult compileAndEvaluate(
      String constraint,
      MetamodelWrapperInterface wrapper,
      List<Warning> loaderWarnings,
      List<EChange<EObject>> transaction,
      boolean transactionSupported) {
    // The transaction-less VitruvOCLCompiler constructor (not the 3-arg one with an empty list)
    // must be used when the caller has no notion of a transaction at all — otherwise pre/post
    // blocks would be evaluated with vacuous empty-transaction semantics instead of being skipped.
    // See EvaluationVisitor#transactionSupported.
    VitruvOCLCompiler compiler =
        transactionSupported
            ? new VitruvOCLCompiler(wrapper, null, transaction)
            : new VitruvOCLCompiler(wrapper, null);
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

    if (evaluator != null) {
      for (EvaluationVisitor.SkippedConstraint skipped : evaluator.getSkippedConstraints()) {
        warnings.add(new Warning(Warning.WarningType.PRE_POST_SKIPPED, formatSkipNotice(skipped)));
      }
    }

    boolean satisfied = records.isEmpty();
    String constraintName = extractConstraintName(constraint);
    for (EvaluationVisitor.ViolationRecord violation : records) {
      String sourceFile = wrapper.getSourceFileForInstance(violation.instance());
      String filename = sourceFile != null ? sourceFile : "unknown";
      String instanceLabel = describeInstance(violation.instance());
      String message =
          violation.customMessage() != null ? violation.customMessage() : instanceLabel;
      String block =
          formatViolationBlock(
              violation.severity(), constraintName, filename, instanceLabel, message);
      warnings.add(new Warning(Warning.WarningType.CONSTRAINT_VIOLATION, block));
    }

    return new ConstraintResult(constraint, satisfied, compilerErrors, List.of(), warnings);
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
      if (!feature.isMany()) {
        Object value = instance.eGet(feature);
        if (value instanceof String || value instanceof Integer || value instanceof Boolean) {
          parts.add(feature.getName() + "=\"" + value + "\"");
        }
      }
      if (parts.size() >= 3) {
        break;
      }
    }
    sb.append(String.join(", ", parts)).append(")");
    return sb.toString();
  }

  private static final String VIOLATION_SEP = "-".repeat(57);

  private static String formatViolationBlock(
      String severity, String constraintName, String model, String object, String message) {
    return VIOLATION_SEP
        + "\n"
        + "["
        + severity
        + "] "
        + constraintName
        + "\n"
        + "Model   : "
        + model
        + "\n"
        + "Object  : "
        + object
        + "\n"
        + "Message : "
        + message
        + "\n"
        + VIOLATION_SEP;
  }

  /**
   * Stable, greppable prefix identifying a pre/post-skip notice among a {@code ConstraintResult}'s
   * plain-string warnings (the {@code WarningType} itself is not serialized to CLI/JSON output —
   * see {@code VitruvOclCli}). Consumers (e.g. the VS Code extension) match on this prefix to
   * display skip notices distinctly from constraint violations, and to surface them even when the
   * constraint's invariants all passed (a skipped pre/post is not a violation).
   */
  public static final String PRE_POST_SKIPPED_PREFIX = "PRE/POST SKIPPED:";

  private static String formatSkipNotice(EvaluationVisitor.SkippedConstraint skipped) {
    String label =
        skipped.blockName() != null ? skipped.kind() + " " + skipped.blockName() : skipped.kind();
    return PRE_POST_SKIPPED_PREFIX
        + " '"
        + label
        + "' block in context "
        + skipped.contextName()
        + " was not evaluated — pre/post constraints require a transaction, which this"
        + " evaluation mode does not provide. Only invariants (inv) were checked.";
  }

  private static String extractConstraintName(String constraint) {
    java.util.regex.Matcher m =
        java.util.regex.Pattern.compile("(?<!@)\\b(?:inv|pre|post)\\s+(\\w+)\\s*:")
            .matcher(constraint);
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
        constraints.addAll(splitContextBlockByConstraintKeyword(trimmed));
      }
    }
    return constraints;
  }

  /**
   * Splits a single {@code context ...} block further on {@code inv}/{@code pre}/{@code post}
   * boundaries, so that a shared context header followed by several constraint blocks is
   * evaluated as independent units rather than as one indivisible block. Each resulting segment
   * is re-prefixed with the original context header.
   *
   * <p>The negative lookbehind {@code (?<!@)} keeps this from matching {@code pre} inside the
   * {@code @pre} postfix operator (e.g. {@code self.mass@pre}) — without it, a body using
   * {@code @pre} gets split mid-token, splintering {@code self.mass@} and {@code pre == 999} into
   * two broken fragments.
   */
  private static List<String> splitContextBlockByConstraintKeyword(String contextBlock) {
    java.util.regex.Matcher headerMatcher =
        java.util.regex.Pattern.compile("(?<!@)\\b(?:inv|pre|post)\\b").matcher(contextBlock);
    if (!headerMatcher.find()) {
      return List.of(contextBlock);
    }
    String header = contextBlock.substring(0, headerMatcher.start()).trim();

    String[] segments = contextBlock.split("(?<!@)(?=\\b(?:inv|pre|post)\\b)");
    List<String> result = new ArrayList<>();
    for (String segment : segments) {
      String trimmed = segment.trim();
      if (trimmed.isEmpty() || trimmed.startsWith("context")) {
        continue;
      }
      result.add(header + "\n" + trimmed);
    }
    return result.isEmpty() ? List.of(contextBlock) : result;
  }

  private static Path[] collectFiles(Path directory, String... extensions) throws IOException {
    if (!Files.exists(directory) || !Files.isDirectory(directory)) {
      return new Path[0];
    }
    try (Stream<Path> stream = Files.walk(directory)) {
      return stream
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
          .toList()
          .toArray(new Path[0]);
    }
  }

  private static Path[] collectAllFiles(Path directory) throws IOException {
    if (!Files.exists(directory) || !Files.isDirectory(directory)) {
      return new Path[0];
    }
    try (Stream<Path> stream = Files.walk(directory)) {
      return stream.filter(Files::isRegularFile).toList().toArray(new Path[0]);
    }
  }
}
