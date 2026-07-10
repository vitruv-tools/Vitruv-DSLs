/* ******************************************************************************
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

package tools.vitruv.dsls.vitruvocl.vsum;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.change.correspondence.Correspondence;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.dsls.vitruvocl.pipeline.BatchValidationResult;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;
import tools.vitruv.framework.views.ViewSource;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * Tests for VSUM-based constraint evaluation.
 *
 * <p>Uses a mocked {@link InternalVirtualModel} backed by real EMF resources loaded from test
 * files. This validates the full VSUM integration path including metamodel discovery, instance
 * retrieval, and correspondence-based constraint evaluation.
 */
class VSUMConstraintTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");
  private static final Path CORRESPONDENCE_ECORE =
      Path.of("src/test/resources/test-metamodels/correspondence.ecore");

  private static final Path SPACECRAFT_VOYAGER =
      Path.of("src/test/resources/test-models/spacecraft-voyager.spacemission");
  private static final Path SATELLITE_VOYAGER =
      Path.of("src/test/resources/test-models/satellite-voyager.satellitesystem");
  private static final Path CORRESPONDENCES =
      Path.of("src/test/resources/test-models/correspondences.correspondence");

  private static final Path CONSTRAINTS_FILE =
      Path.of("src/test/resources/test-inputs/valid/simple.ocl");

  private InternalVirtualModel mockVsum;

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  @BeforeEach
  void setUp() {
    mockVsum = mock(InternalVirtualModel.class, withSettings().extraInterfaces(ViewSource.class));
  }

  @AfterEach
  void tearDown() {
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
      EcoreUtil.resolveAll(resourceSet);
    }

    return result;
  }

  /**
   * Finds all objects corresponding to {@code source} from the given correspondence resources,
   * optionally filtered by tag. A null {@code tagFilter} means no tag restriction.
   */
  @SuppressWarnings("unchecked")
  private Set<EObject> resolveCorrespondents(
      EObject source, Collection<Resource> corrResources, String tagFilter) {
    Set<EObject> result = new java.util.HashSet<>();
    if (source == null || source.eResource() == null) {
      return result;
    }
    ResourceSet resourceSet = source.eResource().getResourceSet();

    for (Resource corrResource : corrResources) {
      for (EObject root : corrResource.getContents()) {
        if (!root.eClass().getName().equals("Correspondences")) {
          continue;
        }
        var corrFeature = root.eClass().getEStructuralFeature("correspondences");
        List<EObject> correspondences = (List<EObject>) root.eGet(corrFeature);
        for (EObject corr : correspondences) {
          // Tag-Filter anwenden wenn angegeben
          if (tagFilter != null) {
            var tagFeature = corr.eClass().getEStructuralFeature("tag");
            String corrTag = tagFeature != null ? (String) corr.eGet(tagFeature) : null;
            if (!tagFilter.equals(corrTag)) {
              continue;
            }
          }
          var leftFeature = corr.eClass().getEStructuralFeature("leftEObjects");
          var rightFeature = corr.eClass().getEStructuralFeature("rightEObjects");
          List<EObject> lefts =
              ((List<EObject>) corr.eGet(leftFeature))
                  .stream().map(o -> EcoreUtil.resolve(o, resourceSet)).toList();
          List<EObject> rights =
              ((List<EObject>) corr.eGet(rightFeature))
                  .stream().map(o -> EcoreUtil.resolve(o, resourceSet)).toList();
          if (lefts.contains(source)) {
            result.addAll(rights);
          }
          if (rights.contains(source)) {
            result.addAll(lefts);
          }
        }
      }
    }
    return result;
  }

  /**
   * Creates a mock correspondence model view that returns corresponding objects based on the loaded
   * correspondence resource. Supports both tag-filtered and unfiltered lookups.
   */
  @SuppressWarnings("unchecked")
  private EditableCorrespondenceModelView<Correspondence> mockCorrespondenceModel(
      Collection<Resource> correspondenceResources) {
    EditableCorrespondenceModelView<Correspondence> corrModel =
        mock(EditableCorrespondenceModelView.class);

    // getCorrespondingEObjects(EObject) — kein Tag-Filter
    when(corrModel.getCorrespondingEObjects(any(EObject.class)))
        .thenAnswer(
            invocation -> {
              EObject source = invocation.getArgument(0);
              return resolveCorrespondents(source, correspondenceResources, null);
            });

    // getCorrespondingEObjects(EObject, String) — mit Tag-Filter
    when(corrModel.getCorrespondingEObjects(any(EObject.class), any()))
        .thenAnswer(
            invocation -> {
              EObject source = invocation.getArgument(0);
              String tag = invocation.getArgument(1);
              return resolveCorrespondents(source, correspondenceResources, tag);
            });

    // getCorrespondingEObjects(List) — kein Tag
    when(corrModel.getCorrespondingEObjects(any(List.class)))
        .thenAnswer(
            invocation -> {
              List<EObject> sources = invocation.getArgument(0);
              Set<EObject> result = new java.util.HashSet<>();
              for (EObject source : sources) {
                result.addAll(resolveCorrespondents(source, correspondenceResources, null));
              }
              return Set.of(result.toArray(new Object[0]));
            });

    // getCorrespondingEObjects(List, String) — mit Tag
    when(corrModel.getCorrespondingEObjects(any(List.class), any()))
        .thenAnswer(
            invocation -> {
              List<EObject> sources = invocation.getArgument(0);
              String tag = invocation.getArgument(1);
              Set<EObject> result = new java.util.HashSet<>();
              for (EObject source : sources) {
                result.addAll(resolveCorrespondents(source, correspondenceResources, tag));
              }
              return Set.of(result.toArray(new Object[0]));
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
  void testRegisterVSUM() {
    Collection<Resource> resources = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));

    assertFalse(VitruvOCL.hasRegisteredVSUM(), "No VSUM registered initially");
    VitruvOCL.registerVSUM(mockVsum);
    assertTrue(VitruvOCL.hasRegisteredVSUM(), "VSUM should be registered");
  }

  @Test
  void testClearVSUM() {
    Collection<Resource> resources = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));

    VitruvOCL.registerVSUM(mockVsum);
    assertTrue(VitruvOCL.hasRegisteredVSUM());
    VitruvOCL.clearVSUM();
    assertFalse(VitruvOCL.hasRegisteredVSUM(), "VSUM should be cleared");
  }

  @Test
  void testRegisterVSUMNull() {
    assertThrows(IllegalArgumentException.class, () -> VitruvOCL.registerVSUM(null));
  }

  @Test
  void testEvaluateWithoutRegisteredVSUMThrows() {
    assertThrows(
        IllegalStateException.class,
        () -> VitruvOCL.evaluateConstraint("context spaceMission::Spacecraft inv: true"));
  }

  // ---------------------------------------------------------------------------
  // evaluateConstraint(String) against VSUM
  // ---------------------------------------------------------------------------

  @Test
  void testSimpleConstraintAgainstVSUM() {
    Collection<Resource> resources = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context spaceMission::Spacecraft inv: true");

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Simple true constraint should be satisfied");
  }

  @Test
  void testConstraintViolationAgainstVSUM() {
    Collection<Resource> resources = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context spaceMission::Spacecraft inv: false");

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertFalse(result.isSatisfied(), "false constraint should be violated");
  }

  @Test
  void testAttributeConstraintAgainstVSUM() {
    Collection<Resource> resources = loadResources(SPACEMISSION_ECORE, SPACECRAFT_VOYAGER);
    setupMockVsum(resources, mockCorrespondenceModel(List.of()));
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context spaceMission::Spacecraft inv: self.mass > 0");

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Spacecraft mass should be > 0");
  }

  @Test
  void testCrossMetamodelConstraintAgainstVSUM() {
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
  void testEvaluateConstraintsFromFileAgainstVSUM() {
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

  private void setupCorrespondenceVsum() {
    Collection<Resource> modelResources =
        loadResources(
            SPACEMISSION_ECORE,
            SATELLITE_ECORE,
            CORRESPONDENCE_ECORE,
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            CORRESPONDENCES);
    List<Resource> corrResources =
        modelResources.stream()
            .filter(r -> r.getURI().toString().endsWith(".correspondence"))
            .toList();
    setupMockVsum(modelResources, mockCorrespondenceModel(corrResources));
    VitruvOCL.registerVSUM(mockVsum);
  }

  @ParameterizedTest
  @MethodSource("correspondenceSatisfiedConstraints")
  void testCorrespondenceConstraintSatisfied(String constraint) {
    setupCorrespondenceVsum();
    ConstraintResult result = VitruvOCL.evaluateConstraint(constraint);
    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied());
  }

  static Stream<String> correspondenceSatisfiedConstraints() {
    return Stream.of(
        // exists(sat | self ~ sat)
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat | self ~ sat)
        """,
        // select(~).notEmpty()
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(~).notEmpty()
        """,
        // select(~, Tag=...) matching tag
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances()
            .select(~, Tag = "voyager-match").notEmpty()
        """,
        // select(~, Tag=...) wrong tag → isEmpty
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances()
            .select(~, Tag = "no-such-tag").isEmpty()
        """,
        // reject(~, Tag=...) removes match → isEmpty
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances()
            .reject(~, Tag = "voyager-match").isEmpty()
        """);
  }

  @Test
  void testNoCorrespondenceAgainstVSUM() {
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
  // exists(~, Tag=...) against VSUM (two-call test, stays standalone)
  // ---------------------------------------------------------------------------

  @Test
  void testExistsCorrespondenceWithTagAgainstVSUM() {
    Collection<Resource> modelResources =
        loadResources(
            SPACEMISSION_ECORE,
            SATELLITE_ECORE,
            CORRESPONDENCE_ECORE,
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            CORRESPONDENCES);

    List<Resource> corrResources =
        modelResources.stream()
            .filter(r -> r.getURI().toString().endsWith(".correspondence"))
            .toList();

    setupMockVsum(modelResources, mockCorrespondenceModel(corrResources));
    VitruvOCL.registerVSUM(mockVsum);

    // exists(~, Tag="voyager-match") → true
    ConstraintResult resultTrue =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances()
                .exists(~, Tag = "voyager-match")
            """);
    assertTrue(resultTrue.isSuccess(), "Should succeed: " + resultTrue.toDetailedErrorString());
    assertTrue(
        resultTrue.isSatisfied(), "exists(~, Tag='voyager-match') should be true for Voyager");

    // exists(~, Tag="no-such-tag") → false, daher not(...) true
    ConstraintResult resultFalse =
        VitruvOCL.evaluateConstraint(
            """
            context spaceMission::Spacecraft inv:
              not satelliteSystem::Satellite.allInstances()
                .exists(~, Tag = "no-such-tag")
            """);
    assertTrue(resultFalse.isSuccess(), "Should succeed: " + resultFalse.toDetailedErrorString());
    assertTrue(
        resultFalse.isSatisfied(), "exists(~, Tag='no-such-tag') should be false — wrong tag");
  }

  // ---------------------------------------------------------------------------
  // Re-registration
  // ---------------------------------------------------------------------------

  @Test
  void testReRegisterVSUM() {
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
