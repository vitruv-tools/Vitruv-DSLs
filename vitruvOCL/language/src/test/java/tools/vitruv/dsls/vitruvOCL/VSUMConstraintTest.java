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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.dsls.vitruvOCL.pipeline.BatchValidationResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;
import tools.vitruv.framework.views.ViewSource;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * Tests for VSUM-based constraint evaluation.
 *
 * <p>Uses a mocked {@link InternalVirtualModel} backed by real EMF resources loaded from test
 * files. This validates the full VSUM integration path including metamodel discovery, instance
 * retrieval, and correspondence-based constraint evaluation.
 */
public class VSUMConstraintTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");
  private static final Path CORRESPONDENCE_ECORE =
      Path.of("src/test/resources/test-metamodels/correspondence.ecore");

  private static final Path SPACECRAFT_VOYAGER =
      Path.of("src/test/resources/test-models/spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_ATLAS =
      Path.of("src/test/resources/test-models/spacecraft-atlas.spacemission");
  private static final Path SATELLITE_VOYAGER =
      Path.of("src/test/resources/test-models/satellite-voyager.satellitesystem");
  private static final Path SATELLITE_ATLAS =
      Path.of("src/test/resources/test-models/satellite-atlas.satellitesystem");
  private static final Path SATELLITE_HUBBLE =
      Path.of("src/test/resources/test-models/satellite-hubble.satellitesystem");
  private static final Path CORRESPONDENCES =
      Path.of("src/test/resources/test-models/correspondences.correspondence");

  private static final Path CONSTRAINTS_FILE =
      Path.of("src/test/resources/test-inputs/valid/simple.ocl");

  private InternalVirtualModel mockVsum;

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  @BeforeEach
  public void setUp() {
    mockVsum = mock(InternalVirtualModel.class, withSettings().extraInterfaces(ViewSource.class));
  }

  @AfterEach
  public void tearDown() {
    VitruvOCL.clearVSUM();
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Loads EMF resources from the given paths into a shared ResourceSet and returns them. Ecore
   * files must be loaded before XMI files so packages are registered.
   */
  private Collection<Resource> loadResources(Path... paths) {
    ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet
        .getResourceFactoryRegistry()
        .getExtensionToFactoryMap()
        .put("*", new XMIResourceFactoryImpl());

    // Ecores zuerst
    for (Path path : paths) {
      if (path.toString().endsWith(".ecore")) {
        URI uri = URI.createFileURI(path.toAbsolutePath().toString());
        Resource resource = resourceSet.getResource(uri, true);
        if (!resource.getContents().isEmpty()) {
          EObject root = resource.getContents().get(0);
          if (root instanceof org.eclipse.emf.ecore.EPackage pkg) {
            org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
          }
        }
      }
    }

    // Dann Instanzen — mit absolutem Pfad damit relative URIs in der Correspondence aufgelöst
    // werden
    List<Resource> result = new java.util.ArrayList<>();
    for (Path path : paths) {
      if (!path.toString().endsWith(".ecore")) {
        URI uri = URI.createFileURI(path.toAbsolutePath().toString());
        result.add(resourceSet.getResource(uri, true));
      }
    }

    // Proxies auflösen nachdem alle Resources geladen sind
    for (Resource r : resourceSet.getResources()) {
      org.eclipse.emf.ecore.util.EcoreUtil.resolveAll(resourceSet);
    }

    return result;
  }

  /**
   * Creates a mock correspondence model view that returns corresponding objects based on the loaded
   * correspondence resource.
   */
  @SuppressWarnings("unchecked")
  private EditableCorrespondenceModelView<Correspondence> mockCorrespondenceModel(
      Collection<Resource> correspondenceResources) {
    EditableCorrespondenceModelView<Correspondence> corrModel =
        mock(EditableCorrespondenceModelView.class);

    // For each EObject, compute its correspondences from the loaded resources
    when(corrModel.getCorrespondingEObjects(any(EObject.class)))
        .thenAnswer(
            invocation -> {
              EObject source = invocation.getArgument(0);
              ResourceSet resourceSet = source.eResource().getResourceSet();
              Set<EObject> result = new java.util.HashSet<>();
              for (Resource corrResource : correspondenceResources) {
                for (EObject root : corrResource.getContents()) {
                  if (!root.eClass().getName().equals("Correspondences")) continue;
                  var corrFeature = root.eClass().getEStructuralFeature("correspondences");
                  List<EObject> correspondences = (List<EObject>) root.eGet(corrFeature);
                  for (EObject corr : correspondences) {
                    var leftFeature = corr.eClass().getEStructuralFeature("leftEObjects");
                    var rightFeature = corr.eClass().getEStructuralFeature("rightEObjects");
                    List<EObject> left = (List<EObject>) corr.eGet(leftFeature);
                    List<EObject> right = (List<EObject>) corr.eGet(rightFeature);

                    // Proxies über den ResourceSet des source-Objekts auflösen
                    List<EObject> resolvedLeft =
                        left.stream().map(o -> EcoreUtil.resolve(o, resourceSet)).toList();
                    List<EObject> resolvedRight =
                        right.stream().map(o -> EcoreUtil.resolve(o, resourceSet)).toList();

                    if (resolvedLeft.contains(source)) result.addAll(resolvedRight);
                    if (resolvedRight.contains(source)) result.addAll(resolvedLeft);
                  }
                }
              }
              return result;
            });

    return corrModel;
  }

  /** Sets up the mock VSUM with the given model resources and correspondence model. */
  private void setupMockVsum(
      Collection<Resource> modelResources,
      EditableCorrespondenceModelView<Correspondence> corrModel) {
    when(((ViewSource) mockVsum).getViewSourceModels()).thenReturn(modelResources);
    when(mockVsum.getCorrespondenceModel()).thenReturn(corrModel);
  }

  // ---------------------------------------------------------------------------
  // registerVSUM / clearVSUM / hasRegisteredVSUM
  // ---------------------------------------------------------------------------

  @Test
  public void testRegisterVSUM() {
    Collection<Resource> resources = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));

    assertFalse(VitruvOCL.hasRegisteredVSUM(), "No VSUM registered initially");
    VitruvOCL.registerVSUM(mockVsum);
    assertTrue(VitruvOCL.hasRegisteredVSUM(), "VSUM should be registered");
  }

  @Test
  public void testClearVSUM() {
    Collection<Resource> resources = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));

    VitruvOCL.registerVSUM(mockVsum);
    assertTrue(VitruvOCL.hasRegisteredVSUM());
    VitruvOCL.clearVSUM();
    assertFalse(VitruvOCL.hasRegisteredVSUM(), "VSUM should be cleared");
  }

  @Test
  public void testRegisterVSUMNull() {
    assertThrows(IllegalArgumentException.class, () -> VitruvOCL.registerVSUM(null));
  }

  @Test
  public void testEvaluateWithoutRegisteredVSUMThrows() {
    assertThrows(
        IllegalStateException.class,
        () -> VitruvOCL.evaluateConstraint("context spaceMission::Spacecraft inv: true"));
  }

  // ---------------------------------------------------------------------------
  // evaluateConstraint(String) against VSUM
  // ---------------------------------------------------------------------------

  @Test
  public void testSimpleConstraintAgainstVSUM() {
    Collection<Resource> resources = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context spaceMission::Spacecraft inv: true");

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Simple true constraint should be satisfied");
  }

  @Test
  public void testConstraintViolationAgainstVSUM() {
    Collection<Resource> resources = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context spaceMission::Spacecraft inv: false");

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertFalse(result.isSatisfied(), "false constraint should be violated");
  }

  @Test
  public void testAttributeConstraintAgainstVSUM() {
    Collection<Resource> resources = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context spaceMission::Spacecraft inv: self.mass > 0");

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Spacecraft mass should be > 0");
  }

  @Test
  public void testCrossMetamodelConstraintAgainstVSUM() {
    Collection<Resource> resources =
        loadResources(SPACEMISSION_ECORE, SATELLITE_ECORE, SPACECRAFT_VOYAGER, SATELLITE_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber == self.serialNumber
              )
            """);

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Spacecraft SC-001 should find matching Satellite SC-001");
  }

  // ---------------------------------------------------------------------------
  // evaluateConstraints(Path) against VSUM
  // ---------------------------------------------------------------------------

  @Test
  public void testEvaluateConstraintsFromFileAgainstVSUM() throws IOException {
    Collection<Resource> resources = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));
    VitruvOCL.registerVSUM(mockVsum);

    BatchValidationResult results = VitruvOCL.evaluateConstraints(CONSTRAINTS_FILE);

    assertNotNull(results);
    assertFalse(results.getResults().isEmpty(), "Should have evaluated at least one constraint");
  }

  // ---------------------------------------------------------------------------
  // Correspondence operator ~ against VSUM
  // ---------------------------------------------------------------------------

  @Test
  public void testCorrespondenceOperatorAgainstVSUM() {
    // ALLES in denselben ResourceSet laden
    Collection<Resource> modelResources =
        loadResources(
            SPACEMISSION_ECORE,
            SATELLITE_ECORE,
            CORRESPONDENCE_ECORE,
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            CORRESPONDENCES); // Correspondence hier mit rein!

    // corrResources sind dieselben Resources aus demselben ResourceSet
    List<Resource> corrResources =
        modelResources.stream()
            .filter(r -> r.getURI().toString().endsWith(".correspondence"))
            .toList();

    setupMockVsum(modelResources, mockCorrespondenceModel(corrResources));
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat | self ~ sat)
            """);

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Spacecraft should have a corresponding Satellite");
  }

  @Test
  public void testSelectCorrespondenceAgainstVSUM() {
    Collection<Resource> modelResources =
        loadResources(
            SPACEMISSION_ECORE,
            SATELLITE_ECORE,
            CORRESPONDENCE_ECORE,
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);
    Collection<Resource> corrResources =
        loadResources(SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE, CORRESPONDENCES);

    setupMockVsum(modelResources, mockCorrespondenceModel(corrResources));
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(~).notEmpty()
            """);

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "select(~) should find corresponding satellites");
  }

  @Test
  public void testNoCorrespondenceAgainstVSUM() {
    // No correspondence model loaded — all ~ checks should return false
    Collection<Resource> modelResources =
        loadResources(
            SPACEMISSION_ECORE, SATELLITE_ECORE,
            SPACECRAFT_VOYAGER, SATELLITE_VOYAGER);

    setupMockVsum(modelResources, mockCorrespondenceModel(List.of()));
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(~).isEmpty()
            """);

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Without correspondences, select(~) should be empty");
  }

  // ---------------------------------------------------------------------------
  // Re-registration
  // ---------------------------------------------------------------------------

  @Test
  public void testReRegisterVSUM() {
    Collection<Resource> resources1 = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    Collection<Resource> resources2 = loadResources(SATELLITE_ECORE, SATELLITE_VOYAGER);

    InternalVirtualModel mockVsum2 =
        mock(InternalVirtualModel.class, withSettings().extraInterfaces(ViewSource.class));

    EditableCorrespondenceModelView<Correspondence> corrModel1 = mockCorrespondenceModel(List.of());
    EditableCorrespondenceModelView<Correspondence> corrModel2 = mockCorrespondenceModel(List.of());

    setupMockVsum(resources1, corrModel1);
    VitruvOCL.registerVSUM(mockVsum);

    when(((ViewSource) mockVsum2).getViewSourceModels()).thenReturn(resources2);
    when(mockVsum2.getCorrespondenceModel()).thenReturn(corrModel2);
    VitruvOCL.registerVSUM(mockVsum2);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context satelliteSystem::Satellite inv: true");

    assertTrue(
        result.isSuccess(), "Should succeed with new VSUM: " + result.toDetailedErrorString());
  }
}