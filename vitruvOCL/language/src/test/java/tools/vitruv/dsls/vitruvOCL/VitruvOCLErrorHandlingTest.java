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
package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.pipeline.*;

/**
 * Comprehensive test suite for VitruvOCL API error handling and edge cases.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>File error detection and reporting (missing files, invalid paths)
 *   <li>Warning generation (duplicates, unused resources)
 *   <li>Multi-instance constraint evaluation with violation tracking
 *   <li>Compiler error handling (syntax, type, undefined symbols)
 *   <li>Batch constraint evaluation
 *   <li>Project-based evaluation with convention-over-configuration
 *   <li>Error message quality and output formatting
 *   <li>Edge cases (empty constraints, missing resources)
 * </ul>
 */
public class VitruvOCLErrorHandlingTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");

  private static final Path SPACECRAFT_VOYAGER = Path.of("spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_ACTIVE = Path.of("spacecraft-active.spacemission");
  private static final Path SPACECRAFT_INACTIVE = Path.of("spacecraft-inactive.spacemission");

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  // ==================== File Error Tests ====================

  /** Tests that missing metamodel (.ecore) files are detected and reported as file errors. */
  @Test
  public void testMissingEcoreFile() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: true",
            new Path[] {Path.of("nonexistent.ecore")},
            new Path[] {});

    assertFalse(result.isSuccess(), "Should fail with missing ecore");
    assertFalse(result.getFileErrors().isEmpty(), "Should have file errors");
    assertTrue(
        result.getFileErrors().stream()
            .anyMatch(e -> e.getType() == FileError.FileErrorType.NOT_FOUND),
        "Should have NOT_FOUND error");
  }

  /** Tests that missing model instance (.xmi) files are detected and reported. */
  @Test
  public void testMissingXmiFile() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: true",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {Path.of("nonexistent.xmi")});

    assertFalse(result.isSuccess());
    assertTrue(
        result.getFileErrors().stream()
            .anyMatch(e -> e.getPath().toString().contains("nonexistent")));
  }

  /**
   * Tests that constraints referencing unavailable metamodel packages are detected and reported.
   */
  @Test
  public void testMissingMetamodelPackage() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context unknownPackage::Class inv: true",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {});

    assertFalse(result.isSuccess());
    assertTrue(
        result.getFileErrors().stream().anyMatch(e -> e.getMessage().contains("unknownPackage")),
        "Should report missing metamodel package");
  }

  /**
   * Tests that multiple file errors are accumulated and reported together rather than failing fast.
   */
  @Test
  public void testMultipleFileErrors() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: true",
            new Path[] {Path.of("missing1.ecore"), Path.of("missing2.ecore")},
            new Path[] {Path.of("missing.xmi")});

    assertFalse(result.isSuccess());
    assertTrue(result.getFileErrors().size() >= 2, "Should report all missing files at once");
  }

  // ==================== Warning Tests ====================

  /** Tests that duplicate metamodel files generate warnings but don't prevent evaluation. */
  @Test
  public void testDuplicateMetamodelWarning() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: true",
            new Path[] {SPACEMISSION_ECORE, SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess(), "Should succeed despite duplicate");
    assertTrue(result.hasWarnings(), "Should have warnings");
    assertTrue(
        result.getWarnings().stream()
            .anyMatch(w -> w.getType() == Warning.WarningType.DUPLICATE_METAMODEL),
        "Should warn about duplicate metamodel");
  }

  /**
   * Tests that metamodels provided but not referenced in the constraint generate unused warnings.
   */
  @Test
  public void testUnusedMetamodelWarning() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: true",
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.hasWarnings());
    assertTrue(
        result.getWarnings().stream()
            .anyMatch(
                w ->
                    w.getType() == Warning.WarningType.UNUSED_METAMODEL
                        && w.getMessage().contains("satelliteSystem")),
        "Should warn about unused satelliteSystem metamodel");
  }

  /** Tests that constraints evaluated without model instances generate warnings. */
  @Test
  public void testUnusedModelWarning() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: true",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {});

    assertTrue(result.isSuccess());
    assertTrue(
        result.getWarnings().stream()
            .anyMatch(w -> w.getType() == Warning.WarningType.UNUSED_MODEL),
        "Should warn when no model instances provided");
  }

  // ==================== Multi-Instance Constraint Violation Tests ====================

  /**
   * Tests that constraints satisfied by all model instances are reported as satisfied without
   * violation warnings.
   */
  @Test
  public void testAllInstancesSatisfied() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: self.operational",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_ACTIVE, SPACECRAFT_ACTIVE});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "All instances satisfy constraint");
    assertFalse(
        result.getWarnings().stream()
            .anyMatch(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION));
  }

  /**
   * Tests that partial constraint violations are detected and reported with indices of violating
   * instances.
   */
  @Test
  public void testPartialViolation() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: self.operational",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_ACTIVE, SPACECRAFT_INACTIVE});

    assertTrue(result.isSuccess(), "Compilation succeeds");
    assertFalse(result.isSatisfied(), "Not all instances satisfy");

    List<Warning> violations =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .toList();

    assertEquals(1, violations.size(), "Only inactive should violate, not active");
    assertTrue(
        violations.get(0).getMessage().contains("[VIOLATION]"),
        "Should use standard violation format");
    assertTrue(
        violations.get(0).getMessage().contains("Inactive-1")
            || violations.get(0).getMessage().contains("SC-009"),
        "Should identify the inactive instance as violating");
    assertFalse(
        violations.get(0).getMessage().contains("Active-1")
            || violations.get(0).getMessage().contains("SC-008"),
        "Should not report the active instance as violating");
  }

  /** Tests that constraints violated by all instances are correctly reported as unsatisfied. */
  @Test
  public void testAllInstancesViolated() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: self.operational",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_INACTIVE, SPACECRAFT_INACTIVE});

    assertTrue(result.isSuccess());
    assertFalse(result.isSatisfied());

    long violationCount =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .count();

    assertEquals(2, violationCount, "Both inactive instances should each produce a violation");
  }

  /** Tests single-instance constraint violations. */
  @Test
  public void testSingleInstanceViolation() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: self.operational",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_INACTIVE});

    assertTrue(result.isSuccess());
    assertFalse(result.isSatisfied());
  }

  // ==================== Compiler Error Tests ====================

  /** Tests that syntax errors in constraints are detected and reported. */
  @Test
  public void testSyntaxError1() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: $$$ invalid @@@",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_VOYAGER});

    assertFalse(result.isSuccess());
    assertFalse(result.getCompilerErrors().isEmpty(), "Should have syntax errors");
  }

  /** Tests that references to undefined attributes are detected as compiler errors. */
  @Test
  public void testUnknownAttribute() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: self.nonExistentAttribute == 5",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_VOYAGER});

    assertFalse(result.isSuccess());
    assertFalse(result.getCompilerErrors().isEmpty());
  }

  /** Tests that type mismatches in operations are detected as compiler errors. */
  @Test
  public void testTypeError() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: self.name + 5",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_VOYAGER});

    assertFalse(result.isSuccess());
    assertFalse(result.getCompilerErrors().isEmpty(), "Should have type error");
  }

  // ==================== Combined Error Scenarios ====================

  /**
   * Tests that file errors prevent compilation attempts and take precedence over potential compiler
   * errors.
   */
  @Test
  public void testFileErrorStopsCompilation() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: SYNTAX ERROR HERE",
            new Path[] {Path.of("missing.ecore")},
            new Path[] {});

    assertFalse(result.isSuccess());
    assertFalse(result.getFileErrors().isEmpty(), "File errors take precedence");
    assertTrue(
        result.getCompilerErrors().isEmpty(), "Should not attempt compilation with file errors");
  }

  /** Tests that successful evaluation can still produce warnings (e.g., for unused resources). */
  @Test
  public void testWarningsWithSuccessfulEvaluation() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: true",
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
    assertTrue(result.hasWarnings(), "Should have warnings about unused metamodel");
  }

  // ==================== Error Message Quality Tests ====================

  /** Tests that detailed error output includes clear section headers and relevant information. */
  @Test
  public void testDetailedErrorOutput() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context unknownPackage::Class inv: true",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {});

    String errorString = result.toDetailedErrorString();
    assertTrue(errorString.contains("FILE ERRORS"), "Should have clear section headers");
    assertTrue(errorString.contains("unknownPackage"), "Should mention missing package");
  }

  /** Tests that successful constraint satisfaction is clearly indicated in output. */
  @Test
  public void testSuccessOutput() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: true",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_VOYAGER});

    String output = result.toString();
    assertTrue(output.contains("SATISFIED"), "Should indicate satisfaction");
  }

  /** Tests that constraint violations are clearly indicated in output. */
  @Test
  public void testViolationOutput() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: false",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_VOYAGER});

    String output = result.toString();
    assertTrue(output.contains("VIOLATED"), "Should indicate violation");
  }

  // ==================== Edge Cases ====================

  /** Tests that empty constraint strings are properly rejected. */
  @Test
  public void testEmptyConstraint() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint("", new Path[] {SPACEMISSION_ECORE}, new Path[] {});

    assertFalse(result.isSuccess(), "Empty constraint should fail");
  }

  /** Tests that evaluation without any metamodels fails appropriately. */
  @Test
  public void testNoMetamodels() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: true", new Path[] {}, new Path[] {});

    assertFalse(result.isSuccess());
    assertFalse(result.getFileErrors().isEmpty());
  }

  /**
   * Tests that constraints can be evaluated without model instances (vacuously true for universal
   * quantification).
   */
  @Test
  public void testNoModels() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: true",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {});

    assertTrue(result.isSuccess(), "Should succeed with no models");
    assertTrue(result.isSatisfied(), "Vacuously true with no instances");
  }

  /** Tests batch evaluation with constraints that have different satisfaction outcomes. */
  @Test
  public void testBatchEvaluationWithMixedResults() {
    List<String> constraints =
        List.of(
            "context spaceMission::Spacecraft inv: true",
            "context spaceMission::Spacecraft inv: false");

    BatchValidationResult result =
        VitruvOCL.evaluateConstraints(
            constraints, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertEquals(2, result.getResults().size());
    assertEquals(1, result.getSatisfiedConstraints().size());
    assertEquals(1, result.getViolatedConstraints().size());
  }

  /** Tests that duplicate constraints in batch evaluation are detected and warned about. */
  @Test
  public void testDuplicateConstraintDetection() {
    List<String> constraints =
        List.of(
            "context spaceMission::Spacecraft inv: true",
            "context spaceMission::Spacecraft inv: true");

    BatchValidationResult result =
        VitruvOCL.evaluateConstraints(
            constraints, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(
        result.getResults().get(1).getWarnings().stream()
            .anyMatch(w -> w.getType() == Warning.WarningType.DUPLICATE_CONSTRAINT));
  }

  /** Tests that constraints can be loaded from a file with semicolon or newline separation. */
  @Test
  public void testEvaluateFromFile() throws IOException {
    Path tempFile = Files.createTempFile("constraints", ".ocl");
    Files.writeString(
        tempFile,
        "context spaceMission::Spacecraft inv: true;\n"
            + "context spaceMission::Spacecraft inv: self.operational");

    BatchValidationResult result =
        VitruvOCL.evaluateConstraints(
            tempFile, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertEquals(2, result.getResults().size());
    Files.delete(tempFile);
  }

  /** Tests project-based evaluation using convention-over-configuration directory structure. */
  @Test
  public void testEvaluateProject() throws IOException {
    Path projectDir = Path.of("src/test/resources/test-project/");

    BatchValidationResult result = VitruvOCL.evaluateProject(projectDir);

    assertFalse(result.getResults().isEmpty(), "Should have results");
    assertTrue(result.allSucceeded(), "All constraints should compile");

    List<ConstraintResult> satisfied = result.getSatisfiedConstraints();
    assertTrue(satisfied.size() > 0, "Should have satisfied constraints");

    assertTrue(
        result.getResults().stream()
            .anyMatch(r -> r.getConstraint().contains("serialNumberMatch")));
    assertTrue(
        result.getResults().stream().anyMatch(r -> r.getConstraint().contains("massConsistency")));

    String summary = result.getSummary();
    assertTrue(summary.contains("constraint"), "Summary should mention constraints");
  }

  /**
   * Tests that the expected project directory structure (constraints.ocl, metamodels/, instances/)
   * is validated.
   */
  @Test
  public void testProjectStructureValidation() throws IOException {
    Path projectDir = Path.of("src/test/resources/test-project");
    Path mainDir = projectDir.resolve("model/src/main");

    assertTrue(Files.exists(mainDir.resolve("constraints.ocl")));
    assertTrue(Files.exists(mainDir.resolve("ecore")));
    assertTrue(Files.exists(mainDir.resolve("instances")));

    BatchValidationResult result = VitruvOCL.evaluateProject(projectDir);
    assertFalse(result.getResults().isEmpty());
  }

  /** Tests that missing constraints.ocl file causes appropriate exception. */
  @Test
  public void testProjectMissingConstraintsFile() {
    Path projectDir = Path.of("src/test/resources/test-project-invalid");

    assertThrows(
        IOException.class,
        () -> {
          VitruvOCL.evaluateProject(projectDir);
        });
  }

  /** Tests that projects without metamodels directory fail with appropriate error messages. */
  @Test
  public void testProjectMissingMetamodelsDir() throws IOException {
    Path tempProject = Files.createTempDirectory("test-project");
    Path mainDir = tempProject.resolve("model/src/main");
    Files.createDirectories(mainDir);
    Files.writeString(
        mainDir.resolve("constraints.ocl"), "context spaceMission::Spacecraft inv: true");
    Files.createDirectory(mainDir.resolve("instances"));

    BatchValidationResult result = VitruvOCL.evaluateProject(tempProject);

    assertTrue(result.getFailedConstraints().size() > 0, "Should fail without metamodels");

    Files.walk(tempProject)
        .sorted(java.util.Comparator.reverseOrder())
        .forEach(
            p -> {
              try {
                Files.delete(p);
              } catch (Exception e) {
              }
            });
  }

  /** Tests that projects without model instances can still evaluate (vacuously true). */
  @Test
  public void testProjectMissingInstancesDir() throws IOException {
    Path tempProject = Files.createTempDirectory("test-project");
    Path mainDir = tempProject.resolve("model/src/main");
    Files.createDirectories(mainDir);
    Files.writeString(
        mainDir.resolve("constraints.ocl"), "context spaceMission::Spacecraft inv: true");
    Path ecoreDir = Files.createDirectory(mainDir.resolve("ecore"));
    Files.copy(SPACEMISSION_ECORE, ecoreDir.resolve("spaceMission.ecore"));

    BatchValidationResult result = VitruvOCL.evaluateProject(tempProject);

    assertTrue(result.allSucceeded(), "Should succeed without instances (vacuously true)");

    Files.walk(tempProject)
        .sorted(java.util.Comparator.reverseOrder())
        .forEach(
            p -> {
              try {
                Files.delete(p);
              } catch (Exception e) {
              }
            });
  }

  /** Tests that empty constraints files result in zero evaluated constraints. */
  @Test
  public void testProjectEmptyConstraintsFile() throws IOException {
    Path tempProject = Files.createTempDirectory("test-project");
    Path mainDir = tempProject.resolve("model/src/main");
    Files.createDirectories(mainDir);
    Files.createFile(mainDir.resolve("constraints.ocl"));
    Files.createDirectory(mainDir.resolve("ecore"));
    Files.createDirectory(mainDir.resolve("instances"));

    BatchValidationResult result = VitruvOCL.evaluateProject(tempProject);

    assertTrue(result.getResults().isEmpty(), "Empty file should yield no constraints");

    Files.walk(tempProject)
        .sorted(java.util.Comparator.reverseOrder())
        .forEach(
            p -> {
              try {
                Files.delete(p);
              } catch (Exception e) {
              }
            });
  }

  // ==================== Violation Reporting Tests ====================

  /**
   * Tests that a violated constraint reports the concrete violating instance, not the
   * root/container object.
   *
   * <p>Regression test: previously violations were reported on the Mission container instead of the
   * individual Spacecraft instance.
   */
  @Test
  public void testViolationReportedOnCorrectInstance() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv operationalCheck: self.operational",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_INACTIVE});

    assertTrue(result.isSuccess(), "Should compile");
    assertFalse(result.isSatisfied(), "Should be violated");

    List<Warning> violations =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .toList();

    assertEquals(1, violations.size(), "Should have exactly one violation");

    String message = violations.get(0).getMessage();
    assertTrue(message.contains("[VIOLATION]"), "Violation should use standard format");
    assertTrue(message.contains("operationalCheck"), "Violation should include constraint name");
    assertTrue(message.contains("Spacecraft"), "Violation should reference Spacecraft");
    assertFalse(
        message.contains("Mission"), "Should not report violation on the Mission container");
  }

  /**
   * Tests that when multiple instances violate a constraint, each produces a separate violation
   * warning — one per violating instance.
   *
   * <p>Regression test: previously only one violation was reported even when multiple instances
   * failed.
   */
  @Test
  public void testMultipleViolationsOnePerInstance() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv operationalCheck: self.operational",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_INACTIVE, SPACECRAFT_INACTIVE});

    assertTrue(result.isSuccess(), "Should compile");
    assertFalse(result.isSatisfied(), "Should be violated");

    long violationCount =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .count();

    assertEquals(2, violationCount, "Should have one violation per violating instance");
  }

  /**
   * Tests that when only some instances violate a constraint, only the violating ones are reported
   * — satisfied instances produce no violation warning.
   */
  @Test
  public void testPartialViolationOnlyReportsViolatingInstances() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv operationalCheck: self.operational",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_ACTIVE, SPACECRAFT_INACTIVE});

    assertTrue(result.isSuccess(), "Should compile");
    assertFalse(result.isSatisfied(), "Should be violated");

    List<Warning> violations =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .toList();

    assertEquals(1, violations.size(), "Should have exactly one violation, not two");

    String message = violations.get(0).getMessage();
    assertTrue(
        message.contains("Inactive"), "Should reference the inactive instance, not the active one");
  }

  /**
   * Tests that violation messages include identifying attribute values of the violating instance,
   * enabling users to locate it in their model files.
   */
  @Test
  public void testViolationMessageContainsInstanceAttributes() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv operationalCheck: self.operational",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_INACTIVE});

    assertTrue(result.isSuccess(), "Should compile");

    List<Warning> violations =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .toList();

    assertFalse(violations.isEmpty(), "Should have violations");

    String message = violations.get(0).getMessage();
    // describeInstance() renders: Spacecraft(name="Inactive-1", serialNumber="SC-009", mass="650")
    assertTrue(
        message.contains("Inactive-1") || message.contains("SC-009"),
        "Violation message should contain identifying attributes of the violating instance");
  }

  /**
   * Tests that violation messages include the source filename so users can locate the file
   * containing the violating instance.
   */
  @Test
  public void testViolationMessageContainsSourceFile() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv operationalCheck: self.operational",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_INACTIVE});

    assertTrue(result.isSuccess(), "Should compile");

    List<Warning> violations =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .toList();

    assertFalse(violations.isEmpty(), "Should have violations");

    String message = violations.get(0).getMessage();
    assertTrue(
        message.contains("spacecraft-inactive"),
        "Violation message should contain the source filename");
  }

  /**
   * Tests that satisfied constraints produce no violation warnings at all, even when multiple
   * instances are evaluated.
   */
  @Test
  public void testNoViolationWarningsWhenSatisfied() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv operationalCheck: self.operational",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_ACTIVE, SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess(), "Should compile");
    assertTrue(result.isSatisfied(), "Should be satisfied");

    long violationCount =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .count();

    assertEquals(
        0, violationCount, "Should have no violation warnings when constraint is satisfied");
  }

  // ==================== Constraint Outcome Tests (based on real VS Code plugin output)
  // ====================

  private static final Path SATELLITE_ECORE_2 =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");

  private static final Path SATELLITE_VOYAGER =
      Path.of("src/test/resources/test-models/satellite-voyager.satellitesystem");
  private static final Path SATELLITE_ATLAS =
      Path.of("src/test/resources/test-models/satellite-atlas.satellitesystem");
  private static final Path SATELLITE_HUBBLE =
      Path.of("src/test/resources/test-models/satellite-hubble.satellitesystem");

  private static final Path[] BOTH_ECORES = new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE_2};

  /**
   * Tests serialNumberMatch: Spacecraft whose serialNumber does not appear in any Satellite must be
   * reported as violated — one violation per non-matching Spacecraft instance.
   *
   * <p>satellite-voyager has SC-001, satellite-atlas has SC-002. spacecraft-active has SC-008 → no
   * match → violation expected. spacecraft-voyager has SC-001 → match → no violation.
   */
  @Test
  public void testSerialNumberMatchViolationForNonMatchingSpacecraft() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv serialNumberMatch:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber == self.serialNumber
              )
            """,
            BOTH_ECORES,
            new Path[] {
              SPACECRAFT_ACTIVE, SPACECRAFT_VOYAGER,
              SATELLITE_VOYAGER, SATELLITE_ATLAS
            });

    assertTrue(result.isSuccess(), "Should compile");
    assertFalse(result.isSatisfied(), "SC-008 has no matching satellite");

    List<Warning> violations =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .toList();

    assertEquals(1, violations.size(), "Only the non-matching Spacecraft should be reported");
    assertTrue(
        violations.get(0).getMessage().contains("SC-008")
            || violations.get(0).getMessage().contains("Active-1"),
        "Violation should identify the non-matching Spacecraft instance");
    assertFalse(
        violations.get(0).getMessage().contains("SC-001")
            || violations.get(0).getMessage().contains("Voyager"),
        "SC-001 (Voyager) matches and should not be reported");
  }

  /** Tests serialNumberMatch: when all Spacecraft have matching Satellites, no violations occur. */
  @Test
  public void testSerialNumberMatchSatisfiedWhenAllMatch() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv serialNumberMatch:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber == self.serialNumber
              )
            """,
            BOTH_ECORES,
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess(), "Should compile");
    assertTrue(result.isSatisfied(), "SC-001 matches satellite-voyager");

    long violationCount =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .count();

    assertEquals(0, violationCount, "No violations when all serial numbers match");
  }

  /**
   * Tests serialInclusion: equivalent to serialNumberMatch but using includes() instead of
   * exists(). Non-matching instances each produce exactly one violation.
   */
  @Test
  public void testSerialInclusionViolationPerInstance() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv serialInclusion:
              satelliteSystem::Satellite.allInstances().collect(sat |
                sat.serialNumber
              ).includes(self.serialNumber)
            """,
            BOTH_ECORES,
            new Path[] {
              SPACECRAFT_ACTIVE,
              SPACECRAFT_INACTIVE,
              SPACECRAFT_VOYAGER,
              SATELLITE_VOYAGER,
              SATELLITE_ATLAS
            });

    assertTrue(result.isSuccess(), "Should compile");
    assertFalse(result.isSatisfied(), "SC-008 and SC-009 have no matching satellites");

    List<String> violationMessages =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .map(Warning::getMessage)
            .toList();

    // SC-001 (voyager) matches satellite-voyager → no violation
    // SC-008 (active) has no matching satellite → violation
    // SC-009 (inactive) has no matching satellite → violation
    assertEquals(
        2, violationMessages.size(), "Should have one violation per non-matching instance");

    assertTrue(
        violationMessages.stream().anyMatch(m -> m.contains("SC-008") || m.contains("Active-1")),
        "SC-008 (Active-1) should be reported as violating");
    assertTrue(
        violationMessages.stream().anyMatch(m -> m.contains("SC-009") || m.contains("Inactive-1")),
        "SC-009 (Inactive-1) should be reported as violating");
    assertFalse(
        violationMessages.stream().anyMatch(m -> m.contains("SC-001") || m.contains("Voyager")),
        "SC-001 (Voyager) matches and should not be reported");
  }

  /**
   * Tests andLogic: Spacecraft must both match a satellite serial AND be operational. A Spacecraft
   * that matches serial but is not operational still violates.
   */
  @Test
  public void testAndLogicViolatesWhenNotOperational() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv andLogic:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber == self.serialNumber
              ) and self.operational
            """,
            BOTH_ECORES,
            new Path[] {
              SPACECRAFT_INACTIVE, SPACECRAFT_VOYAGER,
              SATELLITE_VOYAGER, SATELLITE_ATLAS
            });

    assertTrue(result.isSuccess(), "Should compile");
    assertFalse(result.isSatisfied(), "Inactive has no match AND is not operational");

    List<Warning> violations =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .toList();

    assertEquals(1, violations.size(), "Only the inactive/non-matching spacecraft should violate");
    assertTrue(
        violations.get(0).getMessage().contains("Inactive-1")
            || violations.get(0).getMessage().contains("SC-009"),
        "Violation should identify the inactive instance");
    assertFalse(
        violations.get(0).getMessage().contains("SC-001")
            || violations.get(0).getMessage().contains("Voyager"),
        "Voyager matches and is operational — should not be reported");
  }

  /**
   * Tests conditional: if more than 2 satellites exist, Spacecraft must be operational; else mass
   * must be > 0. With 3 satellites, inactive Spacecraft (not operational) violates.
   */
  @Test
  public void testConditionalViolatesInactiveWhenManySatellites() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv conditional:
              if satelliteSystem::Satellite.allInstances().size() > 2
              then self.operational
              else self.mass > 0
              endif
            """,
            BOTH_ECORES,
            new Path[] {
              SPACECRAFT_ACTIVE, SPACECRAFT_INACTIVE, SPACECRAFT_VOYAGER,
              SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE
            });

    assertTrue(result.isSuccess(), "Should compile");
    assertFalse(result.isSatisfied(), "Inactive spacecraft is not operational");

    List<Warning> violations =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .toList();

    // 3 satellites → then-branch: self.operational required
    // active (operational=true) → passes
    // voyager (operational=true) → passes
    // inactive (operational=false) → violates
    assertEquals(1, violations.size(), "Only inactive spacecraft should violate");
    assertTrue(
        violations.get(0).getMessage().contains("Inactive-1")
            || violations.get(0).getMessage().contains("SC-009"),
        "Violation should identify the inactive instance");
  }

  /**
   * Tests conditional: with fewer than 3 satellites, else-branch applies (mass > 0). All spacecraft
   * with positive mass satisfy — no violations.
   */
  @Test
  public void testConditionalSatisfiedWhenFewSatellites() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv conditional:
              if satelliteSystem::Satellite.allInstances().size() > 2
              then self.operational
              else self.mass > 0
              endif
            """,
            BOTH_ECORES,
            new Path[] {
              SPACECRAFT_ACTIVE, SPACECRAFT_INACTIVE,
              SATELLITE_VOYAGER, SATELLITE_ATLAS
            });

    assertTrue(result.isSuccess(), "Should compile");
    // 2 satellites → else-branch: mass > 0
    // active has mass=600, inactive has mass=650 → both satisfy
    assertTrue(result.isSatisfied(), "All spacecraft have mass > 0, else-branch applies");

    long violationCount =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .count();

    assertEquals(0, violationCount, "No violations when else-branch satisfied by all instances");
  }

  /**
   * Tests massConsistency: for all satellites, if massKg matches spacecraft mass, then active
   * status must match operational. With matching masses and matching active/operational flags,
   * constraint should be satisfied.
   */
  @Test
  public void testMassConsistencySatisfied() {
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv massConsistency:
              satelliteSystem::Satellite.allInstances().forAll(sat |
                sat.massKg == self.mass implies sat.active == self.operational
              )
            """,
            BOTH_ECORES,
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess(), "Should compile");
    assertTrue(
        result.isSatisfied(), "Voyager mass 722 != satellite masses → implies vacuously true");

    long violationCount =
        result.getWarnings().stream()
            .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
            .count();

    assertEquals(0, violationCount);
  }
}
