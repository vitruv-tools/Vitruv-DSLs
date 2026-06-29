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
class AnnotationTypeCheckTest {

  private static final Path BRAKESYSTEM_ECORE =
      Path.of("src/test/resources/test-metamodels/brakesystem.ecore");
  private static final Path BRAKESYSTEM_INSTANCE = Path.of("brakesystem.brakesystem");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  @AfterAll
  static void cleanupRegistry() {
    EPackage.Registry.INSTANCE.remove("http://vitruv.tools/brakesystem/model");
    EPackage.Registry.INSTANCE.remove(
        "http://vitruv.tools/metamodels/dsls/reactions/runtime/correspondence/1.0");
    EPackage.Registry.INSTANCE.remove(
        "http://vitruv.tools/metamodels/change/correspondence/1.0");
  }

  @Test
  void testSeverityCriticalAccepted() {
    ConstraintResult result = eval("""
        context brakesystem::BrakeDisk inv:
            @severity CRITICAL
            self.diameterInMM > 0""");
    assertTrue(result.isSuccess(), "Expected success for @severity CRITICAL: " + result.toDetailedErrorString());
  }

  @Test
  void testSeverityWarningAccepted() {
    ConstraintResult result = eval("""
        context brakesystem::BrakeDisk inv:
            @severity WARNING
            self.diameterInMM > 0""");
    assertTrue(result.isSuccess(), "Expected success for @severity WARNING: " + result.toDetailedErrorString());
  }

  @Test
  void testSeverityMajorAccepted() {
    ConstraintResult result = eval("""
        context brakesystem::BrakeDisk inv:
            @severity MAJOR
            self.diameterInMM > 0""");
    assertTrue(result.isSuccess(), "Expected success for @severity MAJOR: " + result.toDetailedErrorString());
  }

  @Test
  void testSeverityMinorAccepted() {
    ConstraintResult result = eval("""
        context brakesystem::BrakeDisk inv:
            @severity MINOR
            self.diameterInMM > 0""");
    assertTrue(result.isSuccess(), "Expected success for @severity MINOR: " + result.toDetailedErrorString());
  }

  @Test
  void testSeverityInfoAccepted() {
    ConstraintResult result = eval("""
        context brakesystem::BrakeDisk inv:
            @severity INFO
            self.diameterInMM > 0""");
    assertTrue(result.isSuccess(), "Expected success for @severity INFO: " + result.toDetailedErrorString());
  }

  @Test
  void testInvalidSeverityReportsError() {
    ConstraintResult result = eval("""
        context brakesystem::BrakeDisk inv:
            @severity BLOCKER
            self.diameterInMM > 0""");
    assertFalse(result.isSuccess(), "Expected failure for unknown severity BLOCKER");
    assertTrue(
        result.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("BLOCKER")),
        "Error should mention 'BLOCKER'");
  }

  @Test
  void testMessageAnnotationAccepted() {
    ConstraintResult result = eval("""
        context brakesystem::BrakeDisk inv:
            @message "Brake disk {self.name} is invalid"
            self.diameterInMM > 0""");
    assertTrue(result.isSuccess(), "Expected success for @message annotation: " + result.toDetailedErrorString());
  }

  @Test
  void testBothAnnotationsAccepted() {
    ConstraintResult result = eval("""
        context brakesystem::BrakeDisk inv:
            @severity WARNING
            @message "Brake disk {self.name} has invalid diameter"
            self.diameterInMM > 0""");
    assertTrue(result.isSuccess(), "Expected success for combined annotations: " + result.toDetailedErrorString());
  }

  @Test
  void testDuplicateSeverityReportsError() {
    ConstraintResult result = eval("""
        context brakesystem::BrakeDisk inv:
            @severity WARNING
            @severity CRITICAL
            self.diameterInMM > 0""");
    assertFalse(result.isSuccess(), "Expected failure for duplicate @severity");
    assertTrue(
        result.getCompilerErrors().stream().anyMatch(e -> e.getMessage().contains("@severity")),
        "Error should mention '@severity'");
  }

  @Test
  void testDuplicateMessageReportsError() {
    ConstraintResult result = eval("""
        context brakesystem::BrakeDisk inv:
            @message "first"
            @message "second"
            self.diameterInMM > 0""");
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
