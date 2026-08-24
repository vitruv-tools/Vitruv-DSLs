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

package tools.vitruv.dsls.vitruvocl.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import tools.vitruv.dsls.vitruvocl.pipeline.BatchValidationResult;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;

/**
 * Command-line interface for VitruvOCL compiler. Outputs JSON for easy integration with IDEs and
 * tools.
 */
@SuppressWarnings("java:S106")
public class VitruvOclCli {

  private static final String JSON_KEY_SUCCESS = "\"success\":";

  /**
   * CLI entry point.
   *
   * @param args command-line arguments; first argument selects the subcommand
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      printUsage();
      System.exit(1);
    }

    String command = args[0];

    try {
      switch (command) {
        case "check" -> checkConstraint(args);
        case "eval" -> evalConstraint(args);
        case "eval-batch" -> evalBatch(args);
        case "version" -> printVersion();
        default -> {
          System.err.println("Unknown command: " + command);
          printUsage();
          System.exit(1);
        }
      }
    } catch (Exception e) {
      System.out.println(
          String.format(
              "{\"success\":false,\"error\":%s}",
              jsonString(e.getClass().getSimpleName() + ": " + e.getMessage())));
      System.exit(1);
    }
  }

  private static void checkConstraint(String[] args) throws IOException {
    CLIArgs parsed = parseArgs(args);
    String constraintText = Files.readString(parsed.constraintFile);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraintText, parsed.ecoreFiles, new Path[0]);

    System.out.println(buildCheckJson(result));
  }

  private static void evalConstraint(String[] args) throws IOException {
    CLIArgs parsed = parseArgs(args);
    String constraintText = Files.readString(parsed.constraintFile);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraintText, parsed.ecoreFiles, parsed.xmiFiles);

    System.out.println(buildEvalJson(result));
  }

  /**
   * Evaluates all constraints in a file and returns individual results for each.
   *
   * <p>Loads the metamodel/instance context once for the whole batch — via {@link
   * VitruvOCL#evaluateConstraints(Path, Path[], Path[], int)}, whose {@code SmartLoader} loading
   * step unions every constraint's dependencies (not just the first one's) before any evaluation
   * starts — and then evaluates the full constraint list through the existing parallel batch path
   * ({@code ConstraintListEvaluator}), instead of the old per-constraint loop that re-loaded the
   * context on every iteration. Per-constraint JSON shape and field content are unchanged; only how
   * the underlying results are produced differs.
   *
   * <p>Delegates constraints-file parsing to {@link VitruvOCL} itself rather than keeping a second,
   * divergent copy of the same splitting logic here — the CLI's own copy was missing {@code import}
   * line filtering, which silently corrupted every constraint's compiled text whenever the file
   * being evaluated declared an {@code import} (breaking compilation of the whole batch).
   */
  private static void evalBatch(String[] args) throws IOException {
    CLIArgs parsed = parseArgs(args);
    Integer threadPoolSize = parseThreadPoolSize(args);

    BatchValidationResult batch =
        threadPoolSize != null
            ? VitruvOCL.evaluateConstraints(
                parsed.constraintFile, parsed.ecoreFiles, parsed.xmiFiles, threadPoolSize)
            : VitruvOCL.evaluateConstraints(
                parsed.constraintFile, parsed.ecoreFiles, parsed.xmiFiles);

    // Build batch result JSON
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"success\":true,");
    json.append("\"constraints\":[");

    List<String> constraintResults = new ArrayList<>();
    for (ConstraintResult result : batch.getResults()) {
      // Extract constraint name from "context X inv NAME:"
      String name = extractConstraintName(result.getConstraint());

      StringBuilder constraintJson = new StringBuilder();
      constraintJson.append("{");
      constraintJson.append("\"name\":").append(jsonString(name)).append(",");
      constraintJson.append(JSON_KEY_SUCCESS).append(result.isSuccess()).append(",");
      constraintJson.append("\"satisfied\":").append(result.isSatisfied());

      if (!result.getCompilerErrors().isEmpty() || !result.getFileErrors().isEmpty()) {
        constraintJson.append(",\"errors\":[");
        constraintJson.append(formatErrorsJson(result, false));
        constraintJson.append("]");
      }

      if (!result.getWarnings().isEmpty()) {
        constraintJson.append(",\"warnings\":[");
        String warnings =
            result.getWarnings().stream()
                .map(w -> jsonString(w.getMessage()))
                .collect(Collectors.joining(","));
        constraintJson.append(warnings);
        constraintJson.append("]");
      }

      constraintJson.append("}");
      constraintResults.add(constraintJson.toString());
    }

    json.append(String.join(",", constraintResults));
    json.append("]}");

    System.out.println(json.toString());
  }

  /**
   * Parses an optional {@code --threads <N>} argument for {@code eval-batch}, controlling how many
   * worker threads {@link VitruvOCL#evaluateConstraints(List, Path[], Path[], int)} distributes
   * constraint evaluation across.
   *
   * @param args full CLI argument array
   * @return the requested thread pool size, or {@code null} if {@code --threads} was not given (in
   *     which case the caller uses {@code evaluateConstraints}'s own default)
   */
  @SuppressWarnings("java:S127")
  private static Integer parseThreadPoolSize(String[] args) {
    for (int i = 2; i < args.length; i++) {
      if ("--threads".equals(args[i]) && i + 1 < args.length) {
        String value = args[++i].trim();
        try {
          int threadPoolSize = Integer.parseInt(value);
          if (threadPoolSize < 1) {
            System.err.println("--threads must be at least 1, got: " + value);
            System.exit(1);
          }
          return threadPoolSize;
        } catch (NumberFormatException e) {
          System.err.println("Invalid --threads value: " + value + " (must be an integer)");
          System.exit(1);
        }
      }
    }
    return null;
  }

  /**
   * Extracts the constraint name from an {@code inv}/{@code pre}/{@code post} {@code NAME:}
   * declaration.
   *
   * @param constraint the OCL constraint source
   * @return the extracted name, or a fallback value if none is found
   */
  public static String extractConstraintName(String constraint) {
    java.util.regex.Matcher matcher =
        java.util.regex.Pattern.compile("(?<!@)\\b(?:inv|pre|post)\\s+(\\w+)\\s*:")
            .matcher(constraint);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "unknown";
  }

  /**
   * Renders both error categories a {@link ConstraintResult} can carry — {@link
   * ConstraintResult#getCompilerErrors()} (source-position parse/type errors) and {@link
   * ConstraintResult#getFileErrors()} (missing/unreadable metamodel or model files, reported once
   * per batch rather than per constraint) — into one JSON array of {@code {line,column,message[,
   * severity]}} objects.
   *
   * <p>Before this method existed, every call site here rendered only {@code
   * getCompilerErrors()}. Whenever a batch failed to load (e.g. a bad {@code --ecore}/{@code
   * --xmi} path), every constraint in it got a {@link ConstraintResult} with populated {@code
   * fileErrors} but empty {@code compilerErrors} — {@link ConstraintResult#isSuccess()} correctly
   * reported {@code false}, but the JSON's {@code errors}/{@code diagnostics} array stayed empty,
   * so a genuine load failure looked identical to "every constraint is broken for no visible
   * reason." File errors have no source position, so {@code line}/{@code column} are {@code 0}.
   *
   * @param includeSeverity whether to add a {@code "severity"} field (file errors are always
   *     reported as {@code ERROR}) — {@code eval-batch}'s per-constraint JSON omits it for
   *     backward compatibility with its existing {@code {line,column,message}} shape, while {@code
   *     check}/{@code eval} already included it for compiler errors.
   */
  private static String formatErrorsJson(ConstraintResult result, boolean includeSeverity) {
    String compilerErrors =
        result.getCompilerErrors().stream()
            .map(
                e ->
                    includeSeverity
                        ? String.format(
                            "{\"line\":%d,\"column\":%d,\"message\":%s,\"severity\":\"%s\"}",
                            e.getLine(), e.getColumn(), jsonString(e.getMessage()), e.getSeverity())
                        : String.format(
                            "{\"line\":%d,\"column\":%d,\"message\":%s}",
                            e.getLine(), e.getColumn(), jsonString(e.getMessage())))
            .collect(Collectors.joining(","));

    String fileErrors =
        result.getFileErrors().stream()
            .map(
                e ->
                    includeSeverity
                        ? String.format(
                            "{\"line\":0,\"column\":0,\"message\":%s,\"severity\":\"ERROR\"}",
                            jsonString(e.toString()))
                        : String.format(
                            "{\"line\":0,\"column\":0,\"message\":%s}", jsonString(e.toString())))
            .collect(Collectors.joining(","));

    if (compilerErrors.isEmpty()) {
      return fileErrors;
    }
    if (fileErrors.isEmpty()) {
      return compilerErrors;
    }
    return compilerErrors + "," + fileErrors;
  }

  private static String buildCheckJson(ConstraintResult result) {
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append(JSON_KEY_SUCCESS).append(result.isSuccess());
    json.append(",\"diagnostics\":[");
    json.append(formatErrorsJson(result, true));
    json.append("]}");

    return json.toString();
  }

  private static String buildEvalJson(ConstraintResult result) {
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append(JSON_KEY_SUCCESS).append(result.isSuccess());
    json.append(",\"satisfied\":").append(result.isSatisfied());

    json.append(",\"errors\":[");
    json.append(formatErrorsJson(result, true));

    json.append("],\"warnings\":[");
    String warnings =
        result.getWarnings().stream()
            .map(w -> jsonString(w.getMessage()))
            .collect(Collectors.joining(","));
    json.append(warnings);

    json.append("]}");

    return json.toString();
  }

  private static String jsonString(String str) {
    if (str == null) {
      return "null";
    }
    return "\""
        + str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
        + "\"";
  }

  @SuppressWarnings("java:S127")
  private static CLIArgs parseArgs(String[] args) {
    if (args.length < 2) {
      System.err.println("Missing constraint file");
      printUsage();
      System.exit(1);
    }

    Path constraintFile = Path.of(args[1]);
    Path[] ecoreFiles = new Path[0];
    Path[] xmiFiles = new Path[0];

    for (int i = 2; i < args.length; i++) {
      if ("--ecore".equals(args[i]) && i + 1 < args.length) {
        ecoreFiles =
            Arrays.stream(args[++i].split(","))
                .map(String::trim)
                .map(Path::of)
                .toArray(Path[]::new);
      } else if ("--xmi".equals(args[i]) && i + 1 < args.length) {
        xmiFiles =
            Arrays.stream(args[++i].split(","))
                .map(String::trim)
                .map(Path::of)
                .toArray(Path[]::new);
      }
    }

    return new CLIArgs(constraintFile, ecoreFiles, xmiFiles);
  }

  private static void printUsage() {
    System.out.println(
        """
        VitruvOCL Command Line Interface

        Usage:
          vitruvocl check <constraint-file> --ecore <files>
              Type-check constraint syntax and semantics

          vitruvocl eval <constraint-file> --ecore <files> --xmi <files>
              Evaluate single constraint against model instances

          vitruvocl eval-batch <constraints-file> --ecore <files> --xmi <files> [--threads <N>]
              Evaluate all constraints (in parallel) and return individual results

          vitruvocl version
              Print version information

        Arguments:
          <constraint-file>     Path to .ocl/.vocl file with single constraint
          <constraints-file>    Path to .ocl file with multiple constraints
          --ecore <files>       Comma-separated .ecore metamodel files
          --xmi <files>         Comma-separated model instance files
          --threads <N>         eval-batch only: worker threads for constraint evaluation
                                 (default: number of available processors)

        Examples:
          vitruvocl check constraints.ocl --ecore spacemission.ecore
          vitruvocl eval constraints.ocl --ecore model.ecore --xmi instance.xmi
          vitruvocl eval-batch constraints.ocl --ecore model.ecore --xmi instance.xmi
          vitruvocl eval-batch constraints.ocl --ecore model.ecore --xmi instance.xmi --threads 4
        """);
  }

  private static void printVersion() {
    System.out.println("VitruvOCL 1.0.0");
  }

  @SuppressWarnings("java:S6218")
  record CLIArgs(Path constraintFile, Path[] ecoreFiles, Path[] xmiFiles) {}
}
