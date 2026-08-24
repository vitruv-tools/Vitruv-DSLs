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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.composite.description.TransactionalChange;
import tools.vitruv.change.composite.recording.ChangeRecorder;
import tools.vitruv.dsls.vitruvocl.pipeline.BatchValidationResult;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;
import tools.vitruv.framework.views.ViewSource;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;

/**
 * M4 tests: {@code @pre}, {@code OCLisNew}/{@code OCLisModified}/{@code OCLisDeleted} evaluated
 * through the VSUM integration path, mirroring {@link VsumConstraintTest}'s mocked-VSUM setup but
 * with an explicit transaction recorded via {@link ChangeRecorder} against the same live EMF
 * resources the mock VSUM exposes via {@code getViewSourceModels()}.
 */
class VsumTransactionConstraintTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SPACECRAFT_VOYAGER =
      Path.of("src/test/resources/test-models/spacecraft-voyager.spacemission");

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

  /** Loads the spaceMission ecore + the Voyager spacecraft instance into a fresh ResourceSet. */
  private Collection<Resource> loadResources() {
    ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet
        .getResourceFactoryRegistry()
        .getExtensionToFactoryMap()
        .put("*", new XMIResourceFactoryImpl());

    URI ecoreUri = URI.createFileURI(SPACEMISSION_ECORE.toAbsolutePath().toString());
    Resource ecoreResource = resourceSet.getResource(ecoreUri, true);
    EObject root = ecoreResource.getContents().get(0);
    EPackage.Registry.INSTANCE.put(((EPackage) root).getNsURI(), root);

    URI instanceUri = URI.createFileURI(SPACECRAFT_VOYAGER.toAbsolutePath().toString());
    Resource instanceResource = resourceSet.getResource(instanceUri, true);
    EcoreUtil.resolveAll(resourceSet);

    return List.of(instanceResource);
  }

  private void setupMockVsum(Collection<Resource> resources) {
    when(((ViewSource) mockVsum).getViewSourceModels()).thenReturn(resources);
    when(mockVsum.getCorrespondenceModel()).thenReturn(mock(
        tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView.class));
  }

  private EClass spacecraftClass() {
    EPackage pkg = EPackage.Registry.INSTANCE.getEPackage("http://www.example.org/spaceMission");
    EClassifier classifier = pkg.getEClassifier("Spacecraft");
    return (EClass) classifier;
  }

  private static ChangeRecorder record(ResourceSet rs) {
    ChangeRecorder recorder = new ChangeRecorder(rs);
    recorder.addToRecording(rs);
    recorder.beginRecording();
    return recorder;
  }

  private static List<EChange<EObject>> endRecording(ChangeRecorder recorder) {
    TransactionalChange<EObject> change = recorder.endRecording();
    return change.getEChanges();
  }

  // ==================== @pre ====================

  @Test
  void preOperator_reconstructsAttributeValueBeforeTransaction() {
    Collection<Resource> resources = loadResources();
    EObject voyager = resources.iterator().next().getContents().get(0);
    ResourceSet rs = voyager.eResource().getResourceSet();
    EStructuralFeature massFeature = voyager.eClass().getEStructuralFeature("mass");

    ChangeRecorder recorder = record(rs);
    voyager.eSet(massFeature, 900);
    List<EChange<EObject>> transaction = endRecording(recorder);

    setupMockVsum(resources);
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft post: self.mass@pre == 722", transaction);

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Pre-state mass should be 722 (the value before the transaction)");
  }

  @Test
  void preOperator_currentValuePlusDeltaMatchesRecordedChange() {
    Collection<Resource> resources = loadResources();
    EObject voyager = resources.iterator().next().getContents().get(0);
    ResourceSet rs = voyager.eResource().getResourceSet();
    EStructuralFeature massFeature = voyager.eClass().getEStructuralFeature("mass");

    ChangeRecorder recorder = record(rs);
    voyager.eSet(massFeature, 900);
    List<EChange<EObject>> transaction = endRecording(recorder);

    setupMockVsum(resources);
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft post: self.mass == self.mass@pre + 178", transaction);

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "900 should equal 722 (pre) + 178");
  }

  /**
   * Proves that {@code pre}/{@code post} bodies are genuinely evaluated (not a no-op/stub) when a
   * real transaction is supplied: an obviously-false body must be reported as violated, exactly
   * like an equivalent {@code inv} would be. This is the counterpart to {@link
   * #noTransactionOverload_skipsPostBlockAndReportsIt}, which covers the transaction-less CLI/VS
   * Code path where pre/post are skipped outright rather than evaluated — the two paths are easy to
   * conflate, so both are pinned down explicitly.
   */
  @Test
  void postOperator_withRealTransaction_reportsGenuineViolation() {
    Collection<Resource> resources = loadResources();
    EObject voyager = resources.iterator().next().getContents().get(0);
    ResourceSet rs = voyager.eResource().getResourceSet();
    EStructuralFeature massFeature = voyager.eClass().getEStructuralFeature("mass");

    ChangeRecorder recorder = record(rs);
    voyager.eSet(massFeature, 900);
    List<EChange<EObject>> transaction = endRecording(recorder);

    setupMockVsum(resources);
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft post: self.mass@pre == 999", transaction);

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertFalse(
        result.isSatisfied(),
        "Pre-state mass is 722, not 999 — this must be reported as a genuine violation, not"
            + " vacuously satisfied");
  }

  /**
   * Same proof as {@link #postOperator_withRealTransaction_reportsGenuineViolation}, but through
   * the whole-file batch entry point ({@link VitruvOCL#evaluateConstraints(Path, List)}) instead
   * of the single-constraint one. This is the path a Reaction-execution hook uses: it has a real
   * transaction and a {@code .ocl} file with several {@code inv}/{@code pre}/{@code post} blocks
   * to check after one commit, not one constraint string at a time.
   */
  @Test
  void batchFileEvaluation_withRealTransaction_reportsGenuineViolationAlongsidePassingInv(
      @TempDir Path tempDir) throws IOException {
    Collection<Resource> resources = loadResources();
    EObject voyager = resources.iterator().next().getContents().get(0);
    ResourceSet rs = voyager.eResource().getResourceSet();
    EStructuralFeature massFeature = voyager.eClass().getEStructuralFeature("mass");

    ChangeRecorder recorder = record(rs);
    voyager.eSet(massFeature, 900);
    List<EChange<EObject>> transaction = endRecording(recorder);

    setupMockVsum(resources);
    VitruvOCL.registerVSUM(mockVsum);

    Path oclFile = tempDir.resolve("batch.ocl");
    Files.writeString(
        oclFile,
        """
        context spaceMission::Spacecraft inv massIsPositive:
          self.mass > 0
        context spaceMission::Spacecraft post massPreWasWrong:
          self.mass@pre == 999
        """);

    BatchValidationResult result = VitruvOCL.evaluateConstraints(oclFile, transaction);

    assertFalse(
        result.allSatisfied(),
        "The post block's pre-state mass is 722, not 999 — expected a genuine violation: "
            + result.getSummary());
    assertTrue(
        result.getViolatedConstraints().stream()
            .anyMatch(c -> c.getConstraint().contains("massPreWasWrong")),
        "Expected massPreWasWrong to be reported as violated, got: " + result.getSummary());
    assertTrue(
        result.getSatisfiedConstraints().stream()
            .anyMatch(c -> c.getConstraint().contains("massIsPositive")),
        "The sibling inv must still be evaluated and satisfied independently: "
            + result.getSummary());
  }

  @Test
  void noTransactionOverload_skipsPostBlockAndReportsIt() {
    Collection<Resource> resources = loadResources();
    setupMockVsum(resources);
    VitruvOCL.registerVSUM(mockVsum);

    // No transaction argument at all — the pre-existing, transaction-unaware overload used by the
    // CLI and the VS Code plugin (neither has any notion of a transaction). The post block must be
    // skipped outright, not silently evaluated with vacuous empty-transaction semantics — see
    // noTransactionOverload_doesNotFalsePositiveOnLifecyclePredicate below for why that matters.
    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context spaceMission::Spacecraft post: self.mass@pre == 722");

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Skipped post block must not count as a violation");
    assertTrue(
        result.getWarnings().stream()
            .anyMatch(
                w ->
                    w.getMessage().startsWith(VitruvOCL.PRE_POST_SKIPPED_PREFIX)
                        && w.getMessage().contains("post")
                        && w.getMessage().contains("spaceMission::Spacecraft")),
        "Expected a PRE/POST SKIPPED warning naming the block and context, got: "
            + result.getWarnings());
  }

  @Test
  void noTransactionOverload_doesNotFalsePositiveOnLifecyclePredicate() {
    Collection<Resource> resources = loadResources();
    setupMockVsum(resources);
    VitruvOCL.registerVSUM(mockVsum);

    // Without the fix, OCLisModified would evaluate to false with an empty/no-op transaction and
    // get reported as a violated postcondition even though nothing about this constraint is wrong
    // — it simply cannot be checked without a transaction. Skipping it must avoid that false
    // positive.
    ConstraintResult result =
        VitruvOCL.evaluateConstraint("context spaceMission::Spacecraft post: self.OCLisModified");

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "A skipped post block must never be reported as violated");
  }

  @Test
  void noTransactionOverload_stillEvaluatesInvAlongsideSkippedPost() {
    Collection<Resource> resources = loadResources();
    setupMockVsum(resources);
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult passingInv =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: self.mass > 0 post: self.OCLisModified");
    assertTrue(passingInv.isSuccess(), "Should succeed: " + passingInv.toDetailedErrorString());
    assertTrue(passingInv.isSatisfied(), "inv must still be evaluated and satisfied");

    ConstraintResult failingInv =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: self.mass < 0 post: self.OCLisModified");
    assertTrue(failingInv.isSuccess(), "Should succeed: " + failingInv.toDetailedErrorString());
    assertFalse(failingInv.isSatisfied(), "inv must still be evaluated and reported as violated");
  }

  // ==================== OCLisNew / OCLisModified ====================

  @Test
  void oclIsNew_trueOnlyForInstanceCreatedInTransaction() {
    Collection<Resource> resources = loadResources();
    Resource resource = resources.iterator().next();
    ResourceSet rs = resource.getResourceSet();

    ChangeRecorder recorder = record(rs);
    EObject newSpacecraft = EcoreUtil.create(spacecraftClass());
    newSpacecraft.eSet(newSpacecraft.eClass().getEStructuralFeature("serialNumber"), "SC-002");
    newSpacecraft.eSet(newSpacecraft.eClass().getEStructuralFeature("mass"), 500);
    newSpacecraft.eSet(newSpacecraft.eClass().getEStructuralFeature("operational"), true);
    resource.getContents().add(newSpacecraft);
    List<EChange<EObject>> transaction = endRecording(recorder);

    setupMockVsum(resources);
    VitruvOCL.registerVSUM(mockVsum);

    // Every instance must satisfy "OCLisNew implies serialNumber == 'SC-002'" — i.e. OCLisNew is
    // true for exactly the newly created instance and false for the pre-existing Voyager.
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft post: self.OCLisNew implies self.serialNumber =="
                + " \"SC-002\"",
            transaction);

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "OCLisNew should hold only for the newly created spacecraft");
  }

  @Test
  void oclIsModified_trueOnlyForModifiedInstance() {
    Collection<Resource> resources = loadResources();
    Resource resource = resources.iterator().next();
    EObject voyager = resource.getContents().get(0);
    ResourceSet rs = resource.getResourceSet();

    ChangeRecorder recorder = record(rs);
    voyager.eSet(voyager.eClass().getEStructuralFeature("mass"), 900);
    List<EChange<EObject>> transaction = endRecording(recorder);

    setupMockVsum(resources);
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft post: self.OCLisModified", transaction);

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "The only Spacecraft instance was modified in this transaction");
  }

  @Test
  void oclIsNewAggregate_readsCurrentPostValueNotPreState() {
    Collection<Resource> resources = loadResources();
    Resource resource = resources.iterator().next();
    ResourceSet rs = resource.getResourceSet();

    ChangeRecorder recorder = record(rs);
    EObject newSpacecraft = EcoreUtil.create(spacecraftClass());
    newSpacecraft.eSet(newSpacecraft.eClass().getEStructuralFeature("serialNumber"), "SC-002");
    newSpacecraft.eSet(newSpacecraft.eClass().getEStructuralFeature("mass"), 500);
    newSpacecraft.eSet(newSpacecraft.eClass().getEStructuralFeature("operational"), true);
    resource.getContents().add(newSpacecraft);
    List<EChange<EObject>> transaction = endRecording(recorder);

    setupMockVsum(resources);
    VitruvOCL.registerVSUM(mockVsum);

    // A wrong (delta/pre-state) implementation would compare against a value the object never
    // had before creation and fail; the aggregate must read the live post-state value (500).
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft post: self.OCLisNew(mass => 500) implies true",
            transaction);
    ConstraintResult wrongValueResult =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft post: not self.OCLisNew(mass => 999)", transaction);

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied());
    assertTrue(
        wrongValueResult.isSuccess(), "Should succeed: " + wrongValueResult.toDetailedErrorString());
    assertTrue(
        wrongValueResult.isSatisfied(),
        "OCLisNew(mass => 999) must be false — the new spacecraft's mass is 500, not 999");
  }

  // ==================== OCLisDeleted (documented limitation) ====================

  /**
   * {@code self} can, by construction, never be a deleted object: {@code EvaluationVisitor}
   * iterates {@code specification.getAllInstances(eClass)}, which only ever returns currently-live
   * (post-state) instances. So {@code self.OCLisDeleted} is always {@code false} — it only becomes
   * meaningful on a navigated-to (still-reachable) object, which is a narrower scenario than a
   * general-purpose "was this deleted" check. This test documents that limitation rather than
   * hiding it.
   */
  @Test
  void oclIsDeleted_onSelfIsStructurallyAlwaysFalse() {
    Collection<Resource> resources = loadResources();
    Resource resource = resources.iterator().next();
    EObject voyager = resource.getContents().get(0);
    ResourceSet rs = resource.getResourceSet();

    ChangeRecorder recorder = record(rs);
    EObject toDelete = EcoreUtil.create(spacecraftClass());
    toDelete.eSet(toDelete.eClass().getEStructuralFeature("serialNumber"), "SC-DOOMED");
    toDelete.eSet(toDelete.eClass().getEStructuralFeature("mass"), 1);
    toDelete.eSet(toDelete.eClass().getEStructuralFeature("operational"), false);
    resource.getContents().add(toDelete);
    EcoreUtil.delete(toDelete);
    List<EChange<EObject>> transaction = endRecording(recorder);

    setupMockVsum(resources);
    VitruvOCL.registerVSUM(mockVsum);

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft post: not self.OCLisDeleted", transaction);

    assertTrue(result.isSuccess(), "Should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(),
        "self can never be the deleted object — voyager survives and OCLisDeleted is false on it");
    assertFalse(voyager.eIsProxy());
  }
}
