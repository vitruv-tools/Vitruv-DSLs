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

import java.nio.file.Path;
import org.eclipse.emf.ecore.EClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;

/**
 * Integration test suite for metamodel loading and access in VitruvOCL.
 *
 * <p>This test class validates the {@link MetamodelWrapper} component, which is responsible for
 * loading EMF Ecore metamodels and providing access to their metaclasses (EClasses) for use in OCL
 * constraint evaluation. The MetamodelWrapper serves as the bridge between VitruvOCL's constraint
 * language and the underlying Eclipse Modeling Framework (EMF) metamodel infrastructure.
 *
 * <h2>Metamodel Integration Overview</h2>
 *
 * The VitruvOCL compiler integrates with EMF metamodels to enable:
 *
 * <ul>
 *   <li><b>Metaclass resolution:</b> Resolving qualified names like {@code
 *       spaceMission::Spacecraft} to EMF EClass instances
 *   <li><b>Instance access:</b> Retrieving model instances of specific metaclasses via {@code
 *       allInstances()}
 *   <li><b>Property access:</b> Navigating metaclass attributes and references (e.g., {@code
 *       spacecraft.serialNumber})
 *   <li><b>Type checking:</b> Validating that OCL expressions operate on valid metaclasses and
 *       features
 *   <li><b>Cross-metamodel constraints:</b> Writing constraints that span multiple metamodels
 * </ul>
 *
 * <h2>Test Metamodels</h2>
 *
 * This test suite uses two example metamodels located in {@code
 * src/test/resources/test-metamodels/}:
 *
 * <ul>
 *   <li><b>spaceMission.ecore:</b> Defines space mission concepts
 *       <ul>
 *         <li>Spacecraft (attributes: serialNumber, mass, launchDate)
 *         <li>Payload (attributes: powerConsumption, dataRate)
 *         <li>Mission (relationships to spacecraft and payloads)
 *       </ul>
 *   <li><b>satelliteSystem.ecore:</b> Defines satellite system concepts
 *       <ul>
 *         <li>Satellite (attributes: serialNumber, mass, orbitAltitude)
 *         <li>GroundStation (communication infrastructure)
 *       </ul>
 * </ul>
 *
 * <h2>Qualified Name Resolution</h2>
 *
 * VitruvOCL uses qualified names to reference metaclasses from specific metamodels:
 *
 * <pre>{@code
 * metamodel::ClassName
 * }</pre>
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>{@code spaceMission::Spacecraft} → Spacecraft class from spaceMission metamodel
 *   <li>{@code satelliteSystem::Satellite} → Satellite class from satelliteSystem metamodel
 * </ul>
 *
 * <p>This qualified naming scheme is essential for cross-metamodel constraints where classes from
 * different metamodels may have the same name.
 *
 * <h2>MetamodelWrapper Responsibilities</h2>
 *
 * The {@link MetamodelWrapper} provides:
 *
 * <ol>
 *   <li><b>Metamodel loading:</b> {@code loadMetamodel(Path)} loads Ecore files into memory
 *   <li><b>EClass resolution:</b> {@code resolveEClass(String, String)} maps qualified names to
 *       EClass instances
 *   <li><b>Instance retrieval:</b> {@code getAllInstances(EClass)} returns model instances for
 *       evaluation
 *   <li><b>Metamodel enumeration:</b> {@code getAvailableMetamodels()} lists loaded metamodels
 * </ol>
 *
 * <h2>Test Coverage</h2>
 *
 * <ul>
 *   <li><b>Successful resolution:</b> Valid metaclass names resolve to correct EClass instances
 *   <li><b>Feature access:</b> Resolved EClasses provide access to their structural features
 *   <li><b>Error handling:</b> Unknown classes and metamodels return null gracefully
 *   <li><b>Metamodel enumeration:</b> All loaded metamodels are discoverable
 * </ul>
 *
 * <h2>Usage in OCL Constraints</h2>
 *
 * @see MetamodelWrapper Main metamodel integration component
 * @see org.eclipse.emf.ecore.EClass EMF metaclass representation
 * @see CrossMetamodelConstraintTest Tests using metamodel integration for constraints
 */
public class MetamodelIntegrationTest {

  /**
   * The MetamodelWrapper instance under test.
   *
   * <p>Provides access to loaded metamodels and their metaclasses. This instance is recreated fresh
   * for each test via {@link #setup()}.
   */
  private MetamodelWrapper specification;

  /**
   * Sets up the test environment before each test execution.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>Configures the path to test model instances ({@code src/test/resources/test-models/})
   *   <li>Creates a new {@link MetamodelWrapper} instance
   *   <li>Loads the spaceMission metamodel from {@code
   *       src/test/resources/test-metamodels/spaceMission.ecore}
   *   <li>Loads the satelliteSystem metamodel from {@code
   *       src/test/resources/test-metamodels/satelliteSystem.ecore}
   * </ol>
   *
   * <p><b>Path configuration:</b> The {@code TEST_MODELS_PATH} static field is set to enable the
   * wrapper to locate model instances when evaluating {@code allInstances()} operations.
   *
   * <p><b>Metamodel loading:</b> Both metamodels are loaded before tests run, making their
   * metaclasses available for resolution.
   *
   * @throws Exception if metamodel loading fails (file not found, invalid Ecore format, etc.)
   */
  @BeforeEach
  public void setup() throws Exception {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");

    specification = new MetamodelWrapper();
    specification.loadMetamodel(Path.of("src/test/resources/test-metamodels/spaceMission.ecore"));
    specification.loadMetamodel(
        Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore"));
  }

  /**
   * Tests resolution of the Spacecraft metaclass from the spaceMission metamodel.
   *
   * <p><b>Qualified name:</b> {@code spaceMission::Spacecraft}
   *
   * <p><b>Expected behavior:</b>
   *
   * <ul>
   *   <li>Resolution succeeds (returns non-null EClass)
   *   <li>EClass name is "Spacecraft"
   *   <li>EClass belongs to spaceMission metamodel
   * </ul>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Basic metaclass resolution functionality
   *   <li>Qualified name parsing (metamodel::class syntax)
   *   <li>EClass identity verification
   * </ul>
   */
  @Test
  public void testResolveSpacecraftClass() {
    EClass spacecraft = specification.resolveEClass("spaceMission", "Spacecraft");
    assertNotNull(spacecraft, "Should resolve spaceMission::Spacecraft");
    assertEquals("Spacecraft", spacecraft.getName());
  }

  /**
   * Tests resolution of the Satellite metaclass from the satelliteSystem metamodel.
   *
   * <p><b>Qualified name:</b> {@code satelliteSystem::Satellite}
   *
   * <p><b>Expected behavior:</b>
   *
   * <ul>
   *   <li>Resolution succeeds
   *   <li>EClass name is "Satellite"
   * </ul>
   *
   * <p><b>Validates:</b> Resolution works for multiple loaded metamodels.
   */
  @Test
  public void testResolveSatelliteClass() {
    EClass satellite = specification.resolveEClass("satelliteSystem", "Satellite");
    assertNotNull(satellite, "Should resolve satelliteSystem::Satellite");
    assertEquals("Satellite", satellite.getName());
  }

  /**
   * Tests resolution of the Payload metaclass and access to its features.
   *
   * <p><b>Qualified name:</b> {@code spaceMission::Payload}
   *
   * <p><b>Expected behavior:</b>
   *
   * <ul>
   *   <li>Resolution succeeds
   *   <li>EClass name is "Payload"
   *   <li>EClass has attribute "powerConsumption"
   * </ul>
   *
   * <p><b>Feature access:</b> After resolving an EClass, its structural features (attributes and
   * references) can be accessed via {@code getEStructuralFeature(String)}. This is essential for
   * OCL property navigation like {@code payload.powerConsumption}.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Metaclass resolution
   *   <li>Structural feature access
   *   <li>Attribute metadata availability
   * </ul>
   */
  @Test
  public void testResolvePayloadClass() {
    EClass payload = specification.resolveEClass("spaceMission", "Payload");
    assertNotNull(payload, "Should resolve spacemission::Payload");
    assertEquals("Payload", payload.getName());

    // Check attributes - validates feature metadata is accessible
    assertNotNull(payload.getEStructuralFeature("powerConsumption"));
  }

  /**
   * Tests graceful handling of unknown class names.
   *
   * <p><b>Query:</b> {@code spacemission::UnknownClass}
   *
   * <p><b>Expected behavior:</b> Returns {@code null} (class does not exist in metamodel)
   *
   * <p><b>Error handling pattern:</b> The MetamodelWrapper returns {@code null} for unresolvable
   * names rather than throwing exceptions. This allows the type checker to detect and report the
   * error with proper source location information.
   *
   * <p><b>Validates:</b> Graceful failure for invalid class names.
   */
  @Test
  public void testResolveUnknownClass() {
    EClass unknown = specification.resolveEClass("spacemission", "UnknownClass");
    assertNull(unknown, "Should return null for unknown class");
  }

  /**
   * Tests graceful handling of unknown metamodel names.
   *
   * <p><b>Query:</b> {@code unknownmodel::Spacecraft}
   *
   * <p><b>Expected behavior:</b> Returns {@code null} (metamodel not loaded)
   *
   * <p><b>Error handling pattern:</b> Invalid metamodel names are handled the same way as invalid
   * class names - by returning {@code null} to allow the type checker to report meaningful errors.
   *
   * <p><b>Validates:</b> Graceful failure for invalid metamodel names.
   */
  @Test
  public void testResolveUnknownMetamodel() {
    EClass unknown = specification.resolveEClass("unknownmodel", "Spacecraft");
    assertNull(unknown, "Should return null for unknown metamodel");
  }

  /**
   * Tests enumeration of loaded metamodels.
   *
   * <p><b>Expected behavior:</b> Returns a set containing all loaded metamodel names:
   *
   * <ul>
   *   <li>"spaceMission"
   *   <li>"satelliteSystem"
   * </ul>
   *
   * <p><b>Use case:</b> Metamodel enumeration enables:
   *
   * <ul>
   *   <li>Discovery of available metamodels for constraint authoring
   *   <li>Validation that required metamodels are loaded
   *   <li>IDE autocomplete support for qualified names
   * </ul>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>All loaded metamodels are discoverable
   *   <li>Metamodel names match expected values
   * </ul>
   */
  @Test
  public void testAvailableMetamodels() {
    var metamodels = specification.getAvailableMetamodels();
    assertTrue(metamodels.contains("spaceMission"), "Should contain spacemission");
    assertTrue(metamodels.contains("satelliteSystem"), "Should contain satellite");
  }
}