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

import java.nio.file.Path;
import java.util.List;
import tools.vitruv.dsls.vitruvOCL.pipeline.*;

/**
 * Demonstration of VitruvOCL API usage patterns.
 *
 * <p>This class showcases all available methods for evaluating OCL constraints against Ecore
 * models. It demonstrates:
 *
 * <ul>
 *   <li>Convention-over-configuration project evaluation
 *   <li>Explicit file-based constraint evaluation
 *   <li>Programmatic constraint evaluation from strings
 *   <li>Single constraint evaluation
 *   <li>Cross-metamodel constraint checking
 *   <li>Batch result analysis and reporting
 * </ul>
 *
 * <p><b>Project Structure:</b> This example expects the following directory layout:
 *
 * <pre>
 * project/
 *   constraints.ocl              - OCL constraint definitions
 *   metamodels/
 *     spaceMission.ecore         - Metamodel files
 *     satelliteSystem.ecore
 *   instances/
 *     spacecraft-*.spacemission  - Model instances
 *     satellite-*.satellitesystem
 * </pre>
 *
 * <p><b>Required Files:</b>
 *
 * <ul>
 *   <li>{@code constraints.ocl} - Must contain valid OCL constraints starting with {@code context}
 *   <li>{@code metamodels/} directory - Must contain at least one {@code .ecore} file
 *   <li>{@code instances/} directory - Must contain model instance files with appropriate
 *       extensions
 * </ul>
 *
 * @see VitruvOCL for main API documentation
 * @see BatchValidationResult for batch result handling
 * @see ConstraintResult for single constraint results
 */
public class Main {

  /**
   * Main entry point demonstrating all VitruvOCL API methods.
   *
   * <p>Executes examples in sequence, showing progressively more detailed usage patterns from
   * simple project-based evaluation to explicit file handling and programmatic constraint
   * construction.
   *
   * @param args Command line arguments (unused)
   * @throws Exception If file I/O fails or constraints cannot be parsed
   */
  public static void main(String[] args) throws Exception {
    printHeader("VitruvOCL API Demonstration");

    // Execute examples in logical progression
    demoProjectEvaluation();
    demoCustomConstraintLocation();
    demoExplicitFileEvaluation();
    demoProgrammaticConstraints();
    demoSingleConstraint();
    demoCrossMetamodelConstraint();
    demoBatchAnalysis();

    printHeader("Demo Complete");
  }

  /**
   * Demonstrates convention-over-configuration project evaluation.
   *
   * <p>This is the simplest usage pattern - just point to a project directory following the
   * standard structure and VitruvOCL automatically discovers all metamodels, instances, and
   * constraints.
   *
   * <p><b>Required:</b>
   *
   * <ul>
   *   <li>{@code ./constraints.ocl} - Constraint definitions file
   *   <li>{@code ./metamodels/} - Directory with {@code .ecore} files
   *   <li>{@code ./instances/} - Directory with model instance files
   * </ul>
   *
   * <p><b>Output:</b> Summary showing satisfied, violated, and failed constraint counts.
   *
   * @throws Exception If required files are missing or cannot be read
   */
  private static void demoProjectEvaluation() throws Exception {
    printSection("1. Project-Based Evaluation (Convention over Configuration)");

    BatchValidationResult result = VitruvOCL.evaluateProject(Path.of("."));

    System.out.println("Result: " + result.getSummary());
    System.out.println("Detailed Report: \n" + result.getDetailedReport());
  }

  /**
   * Demonstrates project evaluation with custom constraint file location.
   *
   * <p>Similar to {@link #demoProjectEvaluation()}, but allows specifying a different location for
   * the constraints file while still using convention for metamodels and instances.
   *
   * <p><b>Required:</b>
   *
   * <ul>
   *   <li>First argument: Path to constraints file (e.g., {@code constraints.ocl})
   *   <li>Second argument: Root directory containing {@code metamodels/} and {@code instances/}
   *       subdirectories
   * </ul>
   *
   * <p><b>Use Case:</b> When constraint files are managed separately from project resources.
   *
   * @throws Exception If constraint file or resource directories cannot be read
   */
  private static void demoCustomConstraintLocation() throws Exception {
    printSection("2. Custom Constraint File Location");

    BatchValidationResult result =
        VitruvOCL.evaluateProject(Path.of("constraints.ocl"), Path.of("."));

    System.out.println("Result: " + result.getSummary());
    System.out.println();
  }

  /**
   * Demonstrates explicit file-based constraint evaluation.
   *
   * <p>Provides full control over which files to load by explicitly specifying paths to constraint
   * file, metamodel files, and model instance files.
   *
   * <p><b>Required Arguments:</b>
   *
   * <ol>
   *   <li>Constraint file path
   *   <li>Array of metamodel file paths ({@code .ecore} files)
   *   <li>Array of model instance file paths (any EMF-compatible extension)
   * </ol>
   *
   * <p><b>Use Case:</b> When files don't follow standard project structure or when you need to
   * evaluate against specific file subsets.
   *
   * @throws Exception If any specified file cannot be read
   */
  private static void demoExplicitFileEvaluation() throws Exception {
    printSection("3. Explicit File-Based Evaluation");

    // Only use matching instances to demonstrate clean file-based evaluation
    BatchValidationResult result =
        VitruvOCL.evaluateConstraints(
            Path.of("constraints.ocl"),
            new Path[] {
              Path.of("metamodels/spaceMission.ecore"), Path.of("metamodels/satelliteSystem.ecore")
            },
            new Path[] {
              Path.of("instances/spacecraft-voyager.spacemission"),
              Path.of("instances/spacecraft-atlas.spacemission"),
              Path.of("instances/satellite-voyager.satellitesystem"),
              Path.of("instances/satellite-atlas.satellitesystem"),
              Path.of("instances/satellite-hubble.satellitesystem")
            });

    System.out.println("Result: " + result.getSummary());
    System.out.println("Note: Using only matching instances to demonstrate file-based evaluation");
    System.out.println();
  }

  /**
   * Demonstrates programmatic constraint evaluation from string list.
   *
   * <p>Instead of reading constraints from a file, this method shows how to evaluate constraints
   * constructed programmatically as Java strings.
   *
   * <p><b>Required Arguments:</b>
   *
   * <ol>
   *   <li>List of constraint strings (each must start with {@code context})
   *   <li>Array of metamodel file paths
   *   <li>Array of model instance file paths
   * </ol>
   *
   * <p><b>Constraint Format:</b> Each string must be a complete OCL constraint starting with {@code
   * context MetamodelName::ClassName inv: expression}
   *
   * <p><b>Use Case:</b> When constraints are generated dynamically or stored outside of files.
   *
   * @throws Exception If files cannot be read or constraints have syntax errors
   */
  private static void demoProgrammaticConstraints() throws Exception {
    printSection("4. Programmatic Constraint Evaluation");

    List<String> constraints =
        List.of(
            "context spaceMission::Spacecraft inv: self.mass > 0",
            "context spaceMission::Spacecraft inv: self.operational");

    BatchValidationResult result =
        VitruvOCL.evaluateConstraints(
            constraints,
            new Path[] {Path.of("metamodels/spaceMission.ecore")},
            new Path[] {Path.of("instances/spacecraft-voyager.spacemission")});

    System.out.println("Result: " + result.getSummary());
    System.out.println();
  }

  /**
   * Demonstrates single constraint evaluation.
   *
   * <p>Evaluates one constraint directly without reading from file or constructing a list. Returns
   * detailed result including success status and satisfaction status.
   *
   * <p><b>Required Arguments:</b>
   *
   * <ol>
   *   <li>Constraint string (must start with {@code context})
   *   <li>Array of metamodel file paths
   *   <li>Array of model instance file paths
   * </ol>
   *
   * <p><b>Return Value:</b> {@link ConstraintResult} containing:
   *
   * <ul>
   *   <li>{@code isSuccess()} - Whether constraint compiled without errors
   *   <li>{@code isSatisfied()} - Whether all instances satisfy the constraint
   *   <li>{@code getCompilerErrors()} - Syntax or type checking errors
   *   <li>{@code getWarningsSummary()} - Human-readable warning/error summary
   * </ul>
   *
   * <p><b>Use Case:</b> When you need detailed results for a single constraint or want to handle
   * individual constraint failures differently.
   *
   * @throws Exception If files cannot be read
   */
  private static void demoSingleConstraint() throws Exception {
    printSection("5. Single Constraint Evaluation");

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: self.mass > 0",
            new Path[] {Path.of("metamodels/spaceMission.ecore")},
            new Path[] {Path.of("instances/spacecraft-voyager.spacemission")});

    System.out.println("Success: " + result.isSuccess());
    System.out.println("Satisfied: " + result.isSatisfied());
    System.out.println(result.getWarningsSummary());
    System.out.println();
  }

  /**
   * Demonstrates cross-metamodel constraint evaluation.
   *
   * <p>Shows how to write constraints that reference entities from multiple metamodels using {@code
   * allInstances()} to access objects across metamodel boundaries.
   *
   * <p><b>Constraint Pattern:</b> Use fully qualified names to reference classes from other
   * metamodels:
   *
   * <pre>{@code
   * context MetamodelA::ClassA inv:
   *   MetamodelB::ClassB.allInstances().exists(b | condition)
   * }</pre>
   *
   * <p><b>Required:</b>
   *
   * <ul>
   *   <li>All referenced metamodels must be loaded
   *   <li>Both metamodel files in the {@code ecoreFiles} array
   *   <li>Instance files from both metamodels in the {@code xmiFiles} array
   * </ul>
   *
   * <p><b>Use Case:</b> Checking consistency between related models in different metamodels (e.g.,
   * matching IDs, referential integrity, aggregated values).
   *
   * @throws Exception If files cannot be read or constraint has syntax errors
   */
  private static void demoCrossMetamodelConstraint() throws Exception {
    printSection("6. Cross-Metamodel Constraint");

    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber == self.serialNumber
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {
              Path.of("metamodels/spaceMission.ecore"), Path.of("metamodels/satelliteSystem.ecore")
            },
            new Path[] {
              Path.of("instances/spacecraft-voyager.spacemission"),
              Path.of("instances/satellite-voyager.satellitesystem")
            });

    System.out.println("Cross-metamodel constraint satisfied: " + result.isSatisfied());
    System.out.println();
  }

  /**
   * Demonstrates batch result analysis.
   *
   * <p>Shows how to analyze aggregated results from multiple constraint evaluations, including
   * filtering by status and accessing individual results.
   *
   * <p><b>Available Analysis Methods:</b>
   *
   * <ul>
   *   <li>{@code getResults()} - All constraint results
   *   <li>{@code allSucceeded()} - True if no compilation errors occurred
   *   <li>{@code allSatisfied()} - True if all constraints evaluated and were satisfied
   *   <li>{@code getSatisfiedConstraints()} - List of constraints that passed
   *   <li>{@code getViolatedConstraints()} - List of constraints that failed evaluation
   *   <li>{@code getFailedConstraints()} - List of constraints with compilation errors
   *   <li>{@code getSummary()} - Human-readable summary string
   * </ul>
   *
   * <p><b>Use Case:</b> Understanding overall model quality, identifying problem areas, generating
   * reports.
   *
   * @throws Exception If project files cannot be read
   */
  private static void demoBatchAnalysis() throws Exception {
    printSection("7. Batch Result Analysis");

    BatchValidationResult result = VitruvOCL.evaluateProject(Path.of("."));

    System.out.println("Total constraints evaluated: " + result.getResults().size());
    System.out.println("All compiled successfully: " + result.allSucceeded());
    System.out.println("All satisfied: " + result.allSatisfied());
    System.out.println();
    System.out.println("Breakdown:");
    System.out.println("  Satisfied: " + result.getSatisfiedConstraints().size());
    System.out.println("  Violated: " + result.getViolatedConstraints().size());
    System.out.println("  Failed to compile: " + result.getFailedConstraints().size());
    System.out.println();
  }

  /**
   * Prints formatted section header.
   *
   * @param title Section title
   */
  private static void printSection(String title) {
    System.out.println("=== " + title + " ===");
    System.out.println();
  }

  /**
   * Prints formatted main header.
   *
   * @param title Header title
   */
  private static void printHeader(String title) {
    String separator = "=".repeat(title.length() + 8);
    System.out.println();
    System.out.println(separator);
    System.out.println("=== " + title + " ===");
    System.out.println(separator);
    System.out.println();
  }
}