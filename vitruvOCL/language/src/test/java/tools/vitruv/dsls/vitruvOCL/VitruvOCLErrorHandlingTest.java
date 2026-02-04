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
    assertTrue(
        result.getWarnings().stream()
            .anyMatch(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION),
        "Should have constraint violation warning");
    assertTrue(
        result.getWarnings().stream().anyMatch(w -> w.getMessage().contains("indices")),
        "Should indicate which instances violated");
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
    assertTrue(
        result.getWarnings().stream()
            .anyMatch(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION));
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
    Path projectDir = Path.of("src/test/resources/test-project");

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

    assertTrue(Files.exists(projectDir.resolve("constraints.ocl")));
    assertTrue(Files.exists(projectDir.resolve("metamodels")));
    assertTrue(Files.exists(projectDir.resolve("instances")));

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
    Files.writeString(
        tempProject.resolve("constraints.ocl"), "context spaceMission::Spacecraft inv: true");
    Files.createDirectory(tempProject.resolve("instances"));

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
    Files.writeString(
        tempProject.resolve("constraints.ocl"), "context spaceMission::Spacecraft inv: true");
    Path metamodels = Files.createDirectory(tempProject.resolve("metamodels"));
    Files.copy(SPACEMISSION_ECORE, metamodels.resolve("spaceMission.ecore"));

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
    Files.createFile(tempProject.resolve("constraints.ocl"));
    Files.createDirectory(tempProject.resolve("metamodels"));
    Files.createDirectory(tempProject.resolve("instances"));

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
}