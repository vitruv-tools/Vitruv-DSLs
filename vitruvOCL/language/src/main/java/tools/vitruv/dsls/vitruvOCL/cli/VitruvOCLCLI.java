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
package tools.vitruv.dsls.vitruvOCL.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

/**
 * Command-line interface for VitruvOCL constraint evaluation.
 *
 * <p>Provides a JSON-based CLI designed for integration with IDEs, build tools, and scripts. All
 * output is written to stdout as JSON for easy parsing by external tools.
 *
 * <h2>Installation</h2>
 *
 * <pre>
 * # Download the JAR from releases, then run via:
 * java -jar vitruvOCL-0.1.0.jar &lt;command&gt; [arguments]
 * </pre>
 *
 * <h2>Commands</h2>
 *
 * <h3>check - Type-check a constraint</h3>
 *
 * <p>Parses and type-checks a constraint without evaluating it against instances. Useful for syntax
 * validation in IDEs.
 *
 * <pre>
 * java -jar vitruvOCL.jar check &lt;constraint-file&gt; --ecore &lt;files&gt;
 * </pre>
 *
 * Example:
 *
 * <pre>
 * java -jar vitruvOCL.jar check constraint.ocl --ecore spacemission.ecore
 * </pre>
 *
 * Output on success:
 *
 * <pre>{@code
 * {
 *   "success": true,
 *   "diagnostics": []
 * }
 * }</pre>
 *
 * Output on error:
 *
 * <pre>{@code
 * {
 *   "success": false,
 *   "diagnostics": [
 *     {
 *       "line": 2,
 *       "column": 5,
 *       "message": "Unknown property 'mas' on type Spacecraft",
 *       "severity": "ERROR"
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <h3>eval - Evaluate a single constraint</h3>
 *
 * <p>Evaluates a constraint against model instances and returns satisfaction result.
 *
 * <pre>
 * java -jar vitruvOCL.jar eval &lt;constraint-file&gt; --ecore &lt;files&gt; --xmi &lt;files&gt;
 * </pre>
 *
 * Example:
 *
 * <pre>
 * java -jar vitruvOCL.jar eval constraint.ocl \
 *   --ecore spacemission.ecore,satellitesystem.ecore \
 *   --xmi voyager.spacemission,atlas.satellitesystem
 * </pre>
 *
 * Output:
 *
 * <pre>{@code
 * {
 *   "success": true,
 *   "satisfied": true,
 *   "errors": [],
 *   "warnings": []
 * }
 * }</pre>
 *
 * <h3>eval-batch - Evaluate multiple constraints</h3>
 *
 * <p>Evaluates all constraints defined in a constraints file and returns individual results for
 * each. Failed evaluations for individual constraints do not abort the batch - they are reported as
 * error results.
 *
 * <pre>
 * java -jar vitruvOCL.jar eval-batch &lt;constraints-file&gt; --ecore &lt;files&gt; --xmi &lt;files&gt;
 * </pre>
 *
 * Example:
 *
 * <pre>
 * java -jar vitruvOCL.jar eval-batch constraints.ocl \
 *   --ecore spacemission.ecore \
 *   --xmi voyager.spacemission,atlas.satellitesystem
 * </pre>
 *
 * Output:
 *
 * <pre>{@code
 * {
 *   "success": true,
 *   "constraints": [
 *     {
 *       "name": "positiveMass",
 *       "success": true,
 *       "satisfied": true
 *     },
 *     {
 *       "name": "validSerialNumber",
 *       "success": true,
 *       "satisfied": false,
 *       "warnings": ["Constraint violated for instances at indices: [0]"]
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <h3>version - Print version information</h3>
 *
 * <pre>
 * java -jar vitruvOCL.jar version
 * </pre>
 *
 * <h2>Constraint File Format</h2>
 *
 * <pre>
 * -- Comments start with double dash
 * context spaceMission::Spacecraft inv positiveMass:
 *   self.mass > 0
 *
 * context spaceMission::Spacecraft inv validSerialNumber:
 *   self.serialNumber.size() > 0
 * </pre>
 *
 * <p>Constraint names (e.g. {@code positiveMass}) are extracted from the {@code inv} keyword and
 * used to identify constraints in batch results. If no name is provided, the constraint is reported
 * as {@code "unknown"}.
 *
 * <h2>Exit Codes</h2>
 *
 * <ul>
 *   <li>{@code 0} - Command executed successfully (does NOT indicate constraint satisfaction)
 *   <li>{@code 1} - Unexpected error or invalid arguments
 * </ul>
 *
 * <p><b>Note:</b> Both satisfied and violated constraints return exit code 0. Use the {@code
 * "satisfied"} field in the JSON output to determine constraint satisfaction.
 *
 * @see VitruvOCL for the programmatic Java API
 */
public class VitruvOCLCLI {

  /**
   * Main entry point for the VitruvOCL CLI.
   *
   * <p>Dispatches to the appropriate command handler based on the first argument. If no arguments
   * are provided, prints usage information and exits with code 1. Unexpected exceptions are caught
   * and reported as JSON error output.
   *
   * @param args Command-line arguments. First argument must be a valid command ({@code check},
   *     {@code eval}, {@code eval-batch}, {@code version}).
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
          String.format("{\"success\":false,\"error\":%s}", jsonString(e.getMessage())));
      System.exit(1);
    }
  }

  /**
   * Handles the {@code check} command.
   *
   * <p>Reads the constraint file and runs the full compilation pipeline (parse + type check)
   * without evaluating against model instances. Prints a JSON diagnostics report to stdout.
   *
   * @param args Full CLI argument array (args[1] is constraint file path, args[2+] are options)
   * @throws IOException If the constraint file cannot be read
   */
  private static void checkConstraint(String[] args) throws IOException {
    CLIArgs parsed = parseArgs(args);
    String constraintText = Files.readString(parsed.constraintFile);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraintText, parsed.ecoreFiles, new Path[0]);

    System.out.println(buildCheckJson(result));
  }

  /**
   * Handles the {@code eval} command.
   *
   * <p>Reads the constraint file and evaluates it against provided model instances. Prints a JSON
   * result containing success status, satisfaction, errors, and warnings.
   *
   * @param args Full CLI argument array (args[1] is constraint file path, args[2+] are options)
   * @throws IOException If the constraint file cannot be read
   */
  private static void evalConstraint(String[] args) throws IOException {
    CLIArgs parsed = parseArgs(args);
    String constraintText = Files.readString(parsed.constraintFile);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraintText, parsed.ecoreFiles, parsed.xmiFiles);

    System.out.println(buildEvalJson(result));
  }

  /**
   * Handles the {@code eval-batch} command.
   *
   * <p>Parses a constraints file containing multiple constraints, evaluates each independently, and
   * returns a JSON array with individual results per constraint. Failed evaluations for individual
   * constraints do not abort the batch - they are reported as error results.
   *
   * @param args Full CLI argument array (args[1] is constraints file path, args[2+] are options)
   * @throws IOException If the constraints file cannot be read
   */
  private static void evalBatch(String[] args) throws IOException {
    CLIArgs parsed = parseArgs(args);

    List<String> constraints = parseConstraintsFile(parsed.constraintFile);

    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"success\":true,");
    json.append("\"constraints\":[");

    List<String> constraintResults = new ArrayList<>();

    for (String constraint : constraints) {
      try {
        ConstraintResult result =
            VitruvOCL.evaluateConstraint(constraint, parsed.ecoreFiles, parsed.xmiFiles);

        String name = extractConstraintName(constraint);

        StringBuilder constraintJson = new StringBuilder();
        constraintJson.append("{");
        constraintJson.append("\"name\":").append(jsonString(name)).append(",");
        constraintJson.append("\"success\":").append(result.isSuccess()).append(",");
        constraintJson.append("\"satisfied\":").append(result.isSatisfied());

        if (!result.getCompilerErrors().isEmpty()) {
          constraintJson.append(",\"errors\":[");
          String errors =
              result.getCompilerErrors().stream()
                  .map(
                      e ->
                          String.format(
                              "{\"line\":%d,\"column\":%d,\"message\":%s}",
                              e.getLine(), e.getColumn(), jsonString(e.getMessage())))
                  .collect(Collectors.joining(","));
          constraintJson.append(errors);
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

      } catch (Exception e) {
        String name = extractConstraintName(constraint);
        constraintResults.add(
            String.format(
                "{\"name\":%s,\"success\":false,\"error\":%s}",
                jsonString(name), jsonString(e.getMessage())));
      }
    }

    json.append(String.join(",", constraintResults));
    json.append("]}");

    System.out.println(json.toString());
  }

  /**
   * Parses a constraints file into individual constraint strings.
   *
   * <p>Strips comment lines (starting with {@code --}) and splits the remaining content by the
   * {@code context} keyword. Each resulting string represents one complete constraint starting with
   * {@code context}.
   *
   * @param file Path to the constraints file
   * @return List of constraint strings, each starting with {@code context}
   * @throws IOException If the file cannot be read
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
   * Extracts the constraint name from an {@code inv} declaration.
   *
   * <p>Searches for the pattern {@code inv NAME:} and returns the name between the {@code inv}
   * keyword and the colon. Returns {@code "unknown"} if no name can be found (e.g. anonymous
   * constraints like {@code inv:}).
   *
   * <p>Example: given {@code context Spacecraft inv positiveMass:}, returns {@code "positiveMass"}.
   *
   * @param constraint The full constraint string
   * @return The constraint name, or {@code "unknown"} if not found
   */
  private static String extractConstraintName(String constraint) {
    String[] lines = constraint.split("\n");
    for (String line : lines) {
      if (line.contains(" inv ")) {
        int invIndex = line.indexOf(" inv ");
        int colonIndex = line.indexOf(":", invIndex);
        if (colonIndex > invIndex) {
          String name = line.substring(invIndex + 5, colonIndex).trim();
          return name;
        }
      }
    }
    return "unknown";
  }

  /**
   * Builds a JSON response for the {@code check} command.
   *
   * <p>Produces a JSON object with {@code success} flag and a {@code diagnostics} array containing
   * line, column, message, and severity for each compiler error.
   *
   * @param result The constraint evaluation result containing compiler errors
   * @return JSON string with check result
   */
  private static String buildCheckJson(ConstraintResult result) {
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"success\":").append(result.isSuccess());
    json.append(",\"diagnostics\":[");

    String diagnostics =
        result.getCompilerErrors().stream()
            .map(
                e ->
                    String.format(
                        "{\"line\":%d,\"column\":%d,\"message\":%s,\"severity\":\"%s\"}",
                        e.getLine(),
                        e.getColumn(),
                        jsonString(e.getMessage()),
                        e.getSeverity().toString()))
            .collect(Collectors.joining(","));

    json.append(diagnostics);
    json.append("]}");

    return json.toString();
  }

  /**
   * Builds a JSON response for the {@code eval} command.
   *
   * <p>Produces a JSON object with {@code success} flag, {@code satisfied} flag, an {@code errors}
   * array with compiler errors, and a {@code warnings} array with non-fatal issues such as
   * constraint violations or unused metamodels.
   *
   * @param result The constraint evaluation result
   * @return JSON string with evaluation result
   */
  private static String buildEvalJson(ConstraintResult result) {
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"success\":").append(result.isSuccess());
    json.append(",\"satisfied\":").append(result.isSatisfied());

    json.append(",\"errors\":[");
    String errors =
        result.getCompilerErrors().stream()
            .map(
                e ->
                    String.format(
                        "{\"line\":%d,\"column\":%d,\"message\":%s,\"severity\":\"%s\"}",
                        e.getLine(),
                        e.getColumn(),
                        jsonString(e.getMessage()),
                        e.getSeverity().toString()))
            .collect(Collectors.joining(","));
    json.append(errors);

    json.append("],\"warnings\":[");
    String warnings =
        result.getWarnings().stream()
            .map(w -> jsonString(w.getMessage()))
            .collect(Collectors.joining(","));
    json.append(warnings);

    json.append("]}");

    return json.toString();
  }

  /**
   * Escapes a string for safe inclusion in a JSON value.
   *
   * <p>Handles all standard JSON escape sequences including backslash, double quote, newline,
   * carriage return, tab, backspace, and form feed.
   *
   * @param str The string to escape, may be {@code null}
   * @return JSON-safe quoted string, or {@code "null"} if input is {@code null}
   */
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

  /**
   * Parses and validates CLI arguments into a structured {@link CLIArgs} record.
   *
   * <p>Expects at least a constraint file path as the second argument (args[1]). Optional {@code
   * --ecore} and {@code --xmi} flags accept comma-separated file paths.
   *
   * <p>Example:
   *
   * <pre>
   * eval constraint.ocl --ecore model.ecore,other.ecore --xmi instance.xmi
   * </pre>
   *
   * @param args Full CLI argument array including the command name at args[0]
   * @return Parsed {@link CLIArgs} with constraint file, ecore files, and xmi files
   */
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

  /**
   * Prints CLI usage information to stdout.
   *
   * <p>Called when no arguments are provided or an unknown command is encountered.
   */
  private static void printUsage() {
    System.out.println(
        """
        VitruvOCL Command Line Interface

        Usage:
          vitruvocl check <constraint-file> --ecore <files>
              Type-check constraint syntax and semantics

          vitruvocl eval <constraint-file> --ecore <files> --xmi <files>
              Evaluate single constraint against model instances

          vitruvocl eval-batch <constraints-file> --ecore <files> --xmi <files>
              Evaluate all constraints and return individual results

          vitruvocl version
              Print version information

        Arguments:
          <constraint-file>     Path to .ocl/.vocl file with single constraint
          <constraints-file>    Path to .ocl file with multiple constraints
          --ecore <files>       Comma-separated .ecore metamodel files
          --xmi <files>         Comma-separated model instance files

        Examples:
          vitruvocl check constraint.ocl --ecore spacemission.ecore
          vitruvocl eval constraint.ocl --ecore model.ecore --xmi instance.xmi
          vitruvocl eval-batch constraints.ocl --ecore model.ecore --xmi instance.xmi
        """);
  }

  /** Prints the current VitruvOCL version to stdout. */
  private static void printVersion() {
    System.out.println("VitruvOCL 1.0.0");
  }

  /**
   * Structured container for parsed CLI arguments.
   *
   * @param constraintFile Path to the constraint or constraints file
   * @param ecoreFiles Array of metamodel (.ecore) file paths
   * @param xmiFiles Array of model instance file paths
   */
  record CLIArgs(Path constraintFile, Path[] ecoreFiles, Path[] xmiFiles) {}
}