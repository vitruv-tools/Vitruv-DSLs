package tools.vitruv.dsls.vitruvOCL.typechecker;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

/**
 * Tests for @severity and @message annotation type-checking.
 *
 * <p>Uses the brakesystem metamodel so the context type resolves correctly and annotation
 * validation inside visitInvCS is actually exercised.
 */
public class AnnotationTypeCheckTest {

  private static final Path BRAKESYSTEM_ECORE =
      Path.of("src/test/resources/test-metamodels/brakesystem.ecore");
  private static final Path BRAKESYSTEM_INSTANCE = Path.of("brakesystem.brakesystem");

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  @AfterAll
  public static void cleanupRegistry() {
    EPackage.Registry.INSTANCE.remove("http://vitruv.tools/brakesystem/model");
    EPackage.Registry.INSTANCE.remove(
        "http://vitruv.tools/metamodels/dsls/reactions/runtime/correspondence/1.0");
    EPackage.Registry.INSTANCE.remove(
        "http://vitruv.tools/metamodels/change/correspondence/1.0");
  }

  @Test
  public void testSeverityCriticalAccepted() {
    ConstraintResult result = eval(
        "context brakesystem::BrakeDisk inv:\n    @severity CRITICAL\n    self.diameterInMM > 0");
    assertTrue(result.isSuccess(), "Expected success for @severity CRITICAL: " + result.toDetailedErrorString());
  }

  @Test
  public void testSeverityWarningAccepted() {
    ConstraintResult result = eval(
        "context brakesystem::BrakeDisk inv:\n    @severity WARNING\n    self.diameterInMM > 0");
    assertTrue(result.isSuccess(), "Expected success for @severity WARNING: " + result.toDetailedErrorString());
  }

  @Test
  public void testSeverityMajorAccepted() {
    ConstraintResult result = eval(
        "context brakesystem::BrakeDisk inv:\n    @severity MAJOR\n    self.diameterInMM > 0");
    assertTrue(result.isSuccess(), "Expected success for @severity MAJOR: " + result.toDetailedErrorString());
  }

  @Test
  public void testSeverityMinorAccepted() {
    ConstraintResult result = eval(
        "context brakesystem::BrakeDisk inv:\n    @severity MINOR\n    self.diameterInMM > 0");
    assertTrue(result.isSuccess(), "Expected success for @severity MINOR: " + result.toDetailedErrorString());
  }

  @Test
  public void testSeverityInfoAccepted() {
    ConstraintResult result = eval(
        "context brakesystem::BrakeDisk inv:\n    @severity INFO\n    self.diameterInMM > 0");
    assertTrue(result.isSuccess(), "Expected success for @severity INFO: " + result.toDetailedErrorString());
  }

  @Test
  public void testInvalidSeverityReportsError() {
    ConstraintResult result = eval(
        "context brakesystem::BrakeDisk inv:\n    @severity BLOCKER\n    self.diameterInMM > 0");
    assertFalse(result.isSuccess(), "Expected failure for unknown severity BLOCKER");
    assertTrue(
        result.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("BLOCKER")),
        "Error should mention 'BLOCKER'");
  }

  @Test
  public void testMessageAnnotationAccepted() {
    ConstraintResult result = eval(
        "context brakesystem::BrakeDisk inv:\n"
            + "    @message \"Brake disk {self.name} is invalid\"\n"
            + "    self.diameterInMM > 0");
    assertTrue(result.isSuccess(), "Expected success for @message annotation: " + result.toDetailedErrorString());
  }

  @Test
  public void testBothAnnotationsAccepted() {
    ConstraintResult result = eval(
        "context brakesystem::BrakeDisk inv:\n"
            + "    @severity WARNING\n"
            + "    @message \"Brake disk {self.name} has invalid diameter\"\n"
            + "    self.diameterInMM > 0");
    assertTrue(result.isSuccess(), "Expected success for combined annotations: " + result.toDetailedErrorString());
  }

  @Test
  public void testDuplicateSeverityReportsError() {
    ConstraintResult result = eval(
        "context brakesystem::BrakeDisk inv:\n"
            + "    @severity WARNING\n"
            + "    @severity CRITICAL\n"
            + "    self.diameterInMM > 0");
    assertFalse(result.isSuccess(), "Expected failure for duplicate @severity");
    assertTrue(
        result.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("@severity")),
        "Error should mention '@severity'");
  }

  @Test
  public void testDuplicateMessageReportsError() {
    ConstraintResult result = eval(
        "context brakesystem::BrakeDisk inv:\n"
            + "    @message \"first\"\n"
            + "    @message \"second\"\n"
            + "    self.diameterInMM > 0");
    assertFalse(result.isSuccess(), "Expected failure for duplicate @message");
    assertTrue(
        result.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("@message")),
        "Error should mention '@message'");
  }

  private ConstraintResult eval(String constraint) {
    return VitruvOCL.evaluateConstraint(
        constraint,
        new Path[]{BRAKESYSTEM_ECORE},
        new Path[]{BRAKESYSTEM_INSTANCE});
  }
}
