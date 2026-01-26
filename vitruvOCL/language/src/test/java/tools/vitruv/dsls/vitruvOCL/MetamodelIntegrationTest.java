package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.eclipse.emf.ecore.EClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.pipeline.TestConstraintSpecification;

/** Tests for metamodel integration and type resolution. */
public class MetamodelIntegrationTest {

  private TestConstraintSpecification specification;

  @BeforeEach
  public void setup() throws Exception {
    // Set test models path
    TestConstraintSpecification.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");

    // Create specification and load Pfandmodel
    specification = new TestConstraintSpecification();
    specification.loadMetamodel(Path.of("src/test/resources/test-metamodels/pfandmodel.ecore"));
    specification.loadMetamodel(
        Path.of("src/test/resources/test-metamodels/universityModel.ecore"));
  }

  @Test
  public void testResolveFlascheClass() {
    EClass flasche = specification.resolveEClass("pfandmodel", "Flasche");
    assertNotNull(flasche, "Should resolve pfandmodel::Flasche");
    System.out.println("Resolved Flasche class: " + flasche.getName());
    assertEquals("Flasche", flasche.getName());
  }

  @Test
  public void testResolveDoseClass() {
    EClass dose = specification.resolveEClass("pfandmodel", "Dose");
    assertNotNull(dose, "Should resolve pfandmodel::Dose");
    assertEquals("Dose", dose.getName());
  }

  @Test
  public void testResolveKastenClass() {
    EClass kasten = specification.resolveEClass("pfandmodel", "Kasten");
    assertNotNull(kasten, "Should resolve pfandmodel::Kasten");
    assertEquals("Kasten", kasten.getName());

    // Check reference
    assertEquals(1, kasten.getEStructuralFeatures().size());
    assertEquals("enthält", kasten.getEStructuralFeatures().get(0).getName());
  }

  @Test
  public void testResolveUnknownClass() {
    EClass unknown = specification.resolveEClass("pfandmodel", "UnknownClass");
    assertNull(unknown, "Should return null for unknown class");
  }

  @Test
  public void testResolveUnknownMetamodel() {
    EClass unknown = specification.resolveEClass("unknownmodel", "Flasche");
    assertNull(unknown, "Should return null for unknown metamodel");
  }

  @Test
  public void testAvailableMetamodels() {
    var metamodels = specification.getAvailableMetamodels();
    System.out.println("Available metamodels: " + metamodels);
    assertTrue(metamodels.contains("pfandmodel"), "Should contain pfandmodel");
  }
}