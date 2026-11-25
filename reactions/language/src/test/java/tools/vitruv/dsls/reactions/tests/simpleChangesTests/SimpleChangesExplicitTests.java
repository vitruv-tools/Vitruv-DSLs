package tools.vitruv.dsls.reactions.tests.simpleChangesTests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tools.vitruv.dsls.reactions.tests.simpleChangesTests.SimpleChangesTests.SOURCE_MODEL;
import static tools.vitruv.dsls.reactions.tests.simpleChangesTests.SimpleChangesTests.TARGET_MODEL;

import allElementTypes.NonRoot;
import allElementTypes.NonRootObjectContainerHelper;
import allElementTypes.Root;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.testutils.matchers.ModelMatchers;
import tools.vitruv.change.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.dsls.reactions.tests.ExecutionMonitor;
import tools.vitruv.dsls.reactions.tests.ReactionsExecutionTest;
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler;
import tools.vitruv.dsls.reactions.tests.simpleChangesTests.SimpleChangesTestsExecutionMonitor.ChangeType;

class SimpleChangesExplicitTests extends ReactionsExecutionTest {
  private static final String [] nonContainmentNonRootIds = {
      "a", "b", "c"
  };

  @Override
  protected TestReactionsCompiler createCompiler(TestReactionsCompiler.Factory factory) {
    return factory.createCompiler(testReactionsCompilerParameters -> {
      testReactionsCompilerParameters.setReactions(
          List.of("/tools/vitruv/dsls/reactions/tests/AllChangeTypes.reactions"));
      testReactionsCompilerParameters.setChangePropagationSegments(List.of("explicitChangeTests"));
    });
  }

  private static SimpleChangesTestsExecutionMonitor getExecutionMonitor() {
    return SimpleChangesTestsExecutionMonitor.instance;
  }

  private void expectChangeInTargetModel() {
    assertThat(
        resourceAt(TARGET_MODEL),
        ModelMatchers.containsModelOf(resourceAt(SOURCE_MODEL))
    );
  }

  private static void expectChanges(SimpleChangesTestsExecutionMonitor.ChangeType... changes) {
    assertThat(getExecutionMonitor(), ExecutionMonitor.observedExecutions(changes));
  }

  @AfterEach
  void clearEventsFromExecutionMonitor() {
    // The monitor is a singleton: drop events left over from the previous test
    resetExecutionMonitor();
  }

  @BeforeEach
  void createRoot() {
    // Other test classes share the monitor singleton and may leave events behind
    resetExecutionMonitor();
    propagate(resourceAt(SOURCE_MODEL), resource -> {
      Root root = AllElementTypesCreators.aet.Root();
      root.setId("EachTestModelSource");
      NonRootObjectContainerHelper nonRootObjectContainerHelper
          = AllElementTypesCreators.aet.NonRootObjectContainerHelper();
      nonRootObjectContainerHelper.setId("NonRootObjectContainer");
      for (var nonContainmentRootId : nonContainmentNonRootIds) {
        NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
        nonRoot.setId(nonContainmentRootId);
        nonRootObjectContainerHelper.getNonRootObjectsContainment().add(nonRoot);
      }
      root.setNonRootObjectContainerHelper(nonRootObjectContainerHelper);
      resource.getContents().add(root);
    });

    expectChangeInTargetModel();
    expectChanges(ChangeType.CreateEObject);
    getExecutionMonitor().reset();
  }

  private static void resetExecutionMonitor() {
    getExecutionMonitor().reset();
  }

  private NonRoot nonRootWithId(Root rootObject, String searchId) {
    return rootObject.getNonRootObjectContainerHelper()
        .getNonRootObjectsContainment()
        .stream()
        .filter(nonRoot -> nonRoot.getId().equals(searchId))
        .findFirst()
        .orElseThrow();
  }

  private Root getRoot() {
    return from(Root.class, SOURCE_MODEL);
  }

  @Test
  void testUpdateSingleValuedEAttribute() {
    propagate(getRoot(), root -> root.setSingleValuedEAttribute(-1));
    expectChanges(ChangeType.UpdateSingleValuedEAttribute);
    expectChangeInTargetModel();
  }

  @Test
  void testUpdateSingleValuedPrimitiveTypeEAttribute() {
    propagate(getRoot(), root -> root.setSingleValuedPrimitiveTypeEAttribute(-1));
    expectChanges(ChangeType.UpdateSingleValuedPrimitveTypeEAttribute);
    expectChangeInTargetModel();
  }

  @Test
  void testAddMultiValuedEAttribute() {
    propagate(getRoot(), root -> root.getMultiValuedEAttribute().add(1));
    expectChanges(ChangeType.InsertEAttributeValue);
    expectChangeInTargetModel();
  }

  @Test
  void testDeleteMultiValuedEAttribute() {
    propagate(getRoot(), root -> root.getMultiValuedEAttribute().addAll(List.of(1, 2)));
    resetExecutionMonitor();
    propagate(getRoot(), root -> root.getMultiValuedEAttribute().removeFirst());
    expectChanges(ChangeType.RemoveEAttributeValue);
    assertThat(getRoot().getMultiValuedEAttribute().getFirst(), is(2));
    expectChangeInTargetModel();
  }

  @Test
  void testReplaceMultiValuedEAttribute() {
    propagate(getRoot(), root -> root.getMultiValuedEAttribute().addAll(List.of(1, 2)));
    resetExecutionMonitor();
    propagate(getRoot(), root -> root.getMultiValuedEAttribute().set(1, 3));
    expectChanges(ChangeType.RemoveEAttributeValue, ChangeType.InsertEAttributeValue);
    assertThat(getRoot().getMultiValuedEAttribute().get(1), is(3));
    expectChangeInTargetModel();
  }

  @Test
  void testCreateSingleValuedContainmentEReference() {
    propagate(getRoot(), root -> {
      NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
      nonRoot.setId("singleValuedContainmentNonRootTest");
      root.setSingleValuedContainmentEReference(nonRoot);
    });
    expectChanges(ChangeType.CreateEObject, ChangeType.CreateNonRootEObjectSingle);
    expectChangeInTargetModel();
  }

  @Test
  void testDeleteSingleValuedContainmentEReference() {
    propagate(getRoot(), root -> {
      NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
      nonRoot.setId("singleValuedContainmentNonRoot");
      root.setSingleValuedContainmentEReference(nonRoot);
    });
    resetExecutionMonitor();

    propagate(getRoot(), root -> root.setSingleValuedContainmentEReference(null));
    expectChanges(ChangeType.DeleteEObject, ChangeType.DeleteNonRootEObjectSingle);
    expectChangeInTargetModel();
  }

  @Test
  void testReplaceSingleValuedContainmentEReference() {
    propagate(getRoot(), root -> {
      NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
      nonRoot.setId("singleValuedContainmentNonRootBefore");
      root.setSingleValuedContainmentEReference(nonRoot);
    });
    resetExecutionMonitor();

    propagate(getRoot(), root -> {
      NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
      nonRoot.setId("singleValuedContainmentNonRootAfter");
      root.setSingleValuedContainmentEReference(nonRoot);
    });
    expectChanges(
        ChangeType.DeleteNonRootEObjectSingle,
        ChangeType.DeleteEObject,
        ChangeType.CreateNonRootEObjectSingle,
        ChangeType.CreateEObject);
    expectChangeInTargetModel();
  }

  @Test
  void testSetSingleValuedNonContainmentEReference() {
    String testId = nonContainmentNonRootIds[1];
    propagate(getRoot(), root ->
        root.setSingleValuedNonContainmentEReference(nonRootWithId(root, testId)));
    expectChanges(ChangeType.UpdateSingleValuedNonContainmentEReference);
    assertThat(getRoot().getSingleValuedNonContainmentEReference().getId(), is(testId));
    expectChangeInTargetModel();
  }

  @Test
  void testReplaceSingleValuedNonContainmentEReference() {
    propagate(getRoot(), root ->
        root.setSingleValuedNonContainmentEReference(
            nonRootWithId(root, nonContainmentNonRootIds[0])));
    resetExecutionMonitor();

    String testId = nonContainmentNonRootIds[1];
    propagate(getRoot(), root ->
        root.setSingleValuedNonContainmentEReference(nonRootWithId(root, testId)));
    expectChanges(ChangeType.UpdateSingleValuedNonContainmentEReference);
    assertThat(getRoot().getSingleValuedNonContainmentEReference().getId(), is(testId));
    expectChangeInTargetModel();
  }

  @Test
  void testAddMultiValuedContainmentEReference() {
    propagate(getRoot(), root -> {
      NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
      nonRoot.setId("multiValuedContainmentNonRootTest");
      root.getMultiValuedContainmentEReference().add(nonRoot);
    });
    expectChanges(ChangeType.CreateNonRootEObjectInList, ChangeType.CreateEObject);
    expectChangeInTargetModel();
  }

  @Test
  void testInsertMultiValuedNonContainmentEReference() {
    propagate(getRoot(), root -> root.getMultiValuedNonContainmentEReference().add(
        nonRootWithId(root, nonContainmentNonRootIds[0])));
    expectChanges(ChangeType.InsertNonContainmentEReference);
    expectChangeInTargetModel();
  }

  @Test
  void testDeleteMultiValuedContainmentEReference() {
    propagate(getRoot(), root -> {
      NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
      nonRoot.setId("multiValuedContainmentNonRootTest");
      root.getMultiValuedContainmentEReference().add(nonRoot);
    });
    resetExecutionMonitor();

    propagate(getRoot(), root -> root.getMultiValuedContainmentEReference().removeIf(
        (NonRoot nonRoot) -> nonRoot.getId().equals("multiValuedContainmentNonRootTest")));
    expectChanges(ChangeType.DeleteNonRootEObjectInList, ChangeType.DeleteEObject);
    expectChangeInTargetModel();
  }

  @Test
  void testRemoveMultiValuedNonContainmentEReference() {
    propagate(getRoot(), root -> root.getMultiValuedNonContainmentEReference().add(
        nonRootWithId(root, nonContainmentNonRootIds[1])));
    resetExecutionMonitor();
    propagate(getRoot(), root -> root.getMultiValuedNonContainmentEReference().remove(
        nonRootWithId(root, nonContainmentNonRootIds[1])));
    expectChanges(ChangeType.RemoveNonContainmentEReference);
    expectChangeInTargetModel();
  }

  @Test
  void testReplaceMultiValuedNonContainmentEReference() {
    propagate(getRoot(), root -> {
      EList<NonRoot> references = root.getMultiValuedNonContainmentEReference();
      references.add(nonRootWithId(root, nonContainmentNonRootIds[0]));
      references.add(nonRootWithId(root, nonContainmentNonRootIds[1]));
    });
    resetExecutionMonitor();

    propagate(getRoot(), root -> root.getMultiValuedNonContainmentEReference().set(
        1, nonRootWithId(root, nonContainmentNonRootIds[2])));
    expectChanges(
        ChangeType.RemoveNonContainmentEReference,
        ChangeType.InsertNonContainmentEReference);
    Root from = getRoot();
    assertThat(from.getMultiValuedNonContainmentEReference().size(), is(2));
    assertThat(
        from.getMultiValuedNonContainmentEReference().get(1).getId(),
        is(nonContainmentNonRootIds[2]));
    expectChangeInTargetModel();
  }


  @Test
  void testDeleteEachTestModel() {
    assertThat(resourceAt(SOURCE_MODEL), ModelMatchers.exists());
    assertThat(resourceAt(TARGET_MODEL), ModelMatchers.exists());

    propagate(resourceAt(SOURCE_MODEL), resource -> {
      try {
        resource.delete(null);
      } catch (Throwable e) {
        throw Exceptions.sneakyThrow(e);
      }
    });
    assertThat(resourceAt(SOURCE_MODEL), ModelMatchers.doesNotExist());
    assertThat(resourceAt(TARGET_MODEL), ModelMatchers.doesNotExist());
  }

  @Test
  void checkCompilation() {
    assertTrue(true, "Compilation should succeed");
  }
}
