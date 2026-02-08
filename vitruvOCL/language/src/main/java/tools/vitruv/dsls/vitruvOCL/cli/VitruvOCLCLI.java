/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 * SPDX-License-Identifier: EPL-2.0
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
 * Command-line interface for VitruvOCL compiler. Outputs JSON for easy integration with IDEs and
 * tools.
 */
public class VitruvOCLCLI {

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

  /** Evaluates all constraints in a file and returns individual results for each. */
  private static void evalBatch(String[] args) throws IOException {
    CLIArgs parsed = parseArgs(args);

    // Read and parse constraints file
    List<String> constraints = parseConstraintsFile(parsed.constraintFile);

    // Build batch result JSON
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"success\":true,");
    json.append("\"constraints\":[");

    List<String> constraintResults = new ArrayList<>();

    for (String constraint : constraints) {
      try {
        ConstraintResult result =
            VitruvOCL.evaluateConstraint(constraint, parsed.ecoreFiles, parsed.xmiFiles);

        // Extract constraint name from "context X inv NAME:"
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
        // If evaluation fails for this constraint, add error result
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

  private static String extractConstraintName(String constraint) {
    // Extract name from "context ... inv NAME:"
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

  private static void printVersion() {
    System.out.println("VitruvOCL 1.0.0");
  }

  record CLIArgs(Path constraintFile, Path[] ecoreFiles, Path[] xmiFiles) {}
}