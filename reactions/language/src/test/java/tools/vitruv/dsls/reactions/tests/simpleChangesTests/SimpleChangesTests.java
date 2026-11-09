package tools.vitruv.dsls.reactions.tests.simpleChangesTests;

import allElementTypes.NonRoot;
import allElementTypes.NonRootObjectContainerHelper;
import allElementTypes.Root;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.testutils.matchers.ModelMatchers;
import tools.vitruv.change.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.change.testutils.metamodels.TestMetamodelsPathFactory;
import tools.vitruv.dsls.reactions.tests.ExecutionMonitor;
import tools.vitruv.dsls.reactions.tests.ReactionsExecutionTest;
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static tools.vitruv.dsls.reactions.tests.simpleChangesTests.SimpleChangesTestsExecutionMonitor.ChangeType;

class SimpleChangesTests extends ReactionsExecutionTest {
  private static final Path SOURCE_MODEL = TestMetamodelsPathFactory.allElementTypes("SimpleChangeSource");
  private static final Path TARGET_MODEL = TestMetamodelsPathFactory.allElementTypes("SimpleChangeTarget");
  private static final Path FURTHER_SOURCE_MODEL = TestMetamodelsPathFactory.allElementTypes("FurtherSource");
  private static final Path FURTHER_TARGET_MODEL = TestMetamodelsPathFactory.allElementTypes("FurtherTarget");

  private final String[] nonContainmentNonRootIds = { "NonRootHelper0", "NonRootHelper1", "NonRootHelper2" };

  private void expectChangeInTargetModel() {
    assertThat(
        resourceAt(TARGET_MODEL),
        ModelMatchers.containsModelOf(resourceAt(SOURCE_MODEL))
    );
  }

  @Override
  protected TestReactionsCompiler createCompiler(TestReactionsCompiler.Factory factory) {
    return factory.createCompiler((TestReactionsCompiler.TestReactionsCompilerParameters compilerParameters) -> {
      compilerParameters.setReactions(List.of("/tools/vitruv/dsls/reactions/tests/AllElementTypesRedundancy.reactions"));
      compilerParameters.setChangePropagationSegments(List.of("simpleChangesTests"));
    });
  }

  @BeforeEach
  void createRoot() {
    propagate(resourceAt(SOURCE_MODEL), resource -> {
      Root root = AllElementTypesCreators.aet.Root();
      root.setId("EachTestModelSource");
      NonRootObjectContainerHelper nonRootObjectContainerHelper
          = AllElementTypesCreators.aet.NonRootObjectContainerHelper();
      nonRootObjectContainerHelper.setId("NonRootObjectContainer");
      for (var nonContainmentRootId: nonContainmentNonRootIds) {
        NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
        nonRoot.setId(nonContainmentRootId);
        nonRootObjectContainerHelper.getNonRootObjectsContainment().add(nonRoot);
      }
      root.setNonRootObjectContainerHelper(nonRootObjectContainerHelper);
      resource.getContents().add(root);
    });

    expectChangeInTargetModel();
    getExecutionMonitor().reset();
  }

  private static SimpleChangesTestsExecutionMonitor getExecutionMonitor() {
    return SimpleChangesTestsExecutionMonitor.instance;
  }

  private static void expectChanges(ChangeType... changes) {
    assertThat(getExecutionMonitor(), ExecutionMonitor.observedExecutions(changes));
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
        .orElseGet(null);
  }

  private Root getRoot() {
    return from(Root.class, SOURCE_MODEL);
  }

  @Test
  @Disabled("Unset does not produce any change event at the moment")
  void testUnsetSingleValuedEAttribute() {
    propagate(getRoot(),
        root -> root.setSingleValuedEAttribute(null));
    expectChanges(ChangeType.UnsetEAttribute);
    expectChangeInTargetModel();
  }

  @Test
  @Disabled
  void testUnsetSingleValuedNonContainmentEReference() {
    propagate(getRoot(),
        root -> root.setSingleValuedNonContainmentEReference(nonRootWithId(root, nonContainmentNonRootIds[1])));
    resetExecutionMonitor();
    propagate(getRoot(), root ->
        root.setSingleValuedNonContainmentEReference(null));

    expectChanges(ChangeType.UnsetNonContainmentEReference);
    expectChangeInTargetModel();
  }

  @Test
  void testUpdateSingleValuedEAttribute() {
    propagate(getRoot(),
        root -> root.setSingleValuedEAttribute(-1));
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
    NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
    nonRoot.setId("singleValuedContainmentNonRoot");

    propagate(getRoot(), root -> {
      root.setSingleValuedContainmentEReference(nonRoot);
    });
    resetExecutionMonitor();

    propagate(getRoot(), root -> {
      root.setSingleValuedContainmentEReference(null);
    });
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
    propagate(getRoot(), root -> root.setSingleValuedNonContainmentEReference(nonRootWithId(root, testId)));
    expectChanges(ChangeType.UpdateSingleValuedNonContainmentEReference);
    assertThat(getRoot().getSingleValuedNonContainmentEReference().getId(), is(testId));
    expectChangeInTargetModel();
  }

  @Test
  void testReplaceSingleValuedNonContainmentEReference() {
    propagate(getRoot(), root ->
        root.setSingleValuedNonContainmentEReference(nonRootWithId(root, nonContainmentNonRootIds[0])));

    resetExecutionMonitor();

    String testId = nonContainmentNonRootIds[1];
    propagate(getRoot(), root -> root.setSingleValuedNonContainmentEReference(nonRootWithId(root, testId)));

    expectChanges(ChangeType.UpdateSingleValuedNonContainmentEReference);
    assertThat(getRoot().getSingleValuedNonContainmentEReference().getId(), is(testId));
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
    assertThat(getRoot()
        .getMultiValuedEAttribute().getFirst(),
        is(2));
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
  void testDeleteMultiValuedContainmentEReference() {
    propagate(getRoot(), root -> {
      NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
      nonRoot.setId("multiValuedContainmentNonRootTest");
      root.getMultiValuedContainmentEReference().add(nonRoot);
    });
    resetExecutionMonitor();

    propagate(getRoot(), root ->
        root.getMultiValuedContainmentEReference().removeIf(
          (NonRoot nonRoot) -> nonRoot.getId().equals("multiValuedContainmentNonRootTest")
        )
    );
    expectChanges(ChangeType.DeleteNonRootEObjectInList, ChangeType.DeleteEObject);
    expectChangeInTargetModel();
  }

  @Test
  void testReplaceMultiValuedContainmentEReference() {
    propagate(getRoot(), root -> {
      NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
      nonRoot.setId("multiValuedContainmentNonRootBefore");
      root.getMultiValuedContainmentEReference().add(nonRoot);
    });
    resetExecutionMonitor();

    propagate(getRoot(), root -> {
      NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
      nonRoot.setId("multiValuedContainmentNonRootAfter");
      root.getMultiValuedContainmentEReference().set(0, nonRoot);
    });
    expectChanges(
        ChangeType.DeleteNonRootEObjectInList,
        ChangeType.DeleteEObject,
        ChangeType.CreateNonRootEObjectInList,
        ChangeType.CreateEObject);
    assertThat(getRoot().getMultiValuedContainmentEReference().getLast().getId(),
      is("multiValuedContainmentNonRootAfter"));
    expectChangeInTargetModel();
  }

  @Test
  void testInsertMultiValuedNonContainmentEReference() {
    propagate(getRoot(), root -> {
      root.getMultiValuedNonContainmentEReference().add(
          nonRootWithId(root, nonContainmentNonRootIds[0]));
    });
    expectChanges(ChangeType.InsertNonContainmentEReference);
    expectChangeInTargetModel();
  }

  @Test
  void testRemoveMultiValuedNonContainmentEReference() {
    propagate(getRoot(), root -> {
      root.getMultiValuedNonContainmentEReference().add(
          nonRootWithId(root, nonContainmentNonRootIds[1]));
    });
    resetExecutionMonitor();
    propagate(getRoot(), root -> {
      root.getMultiValuedNonContainmentEReference().remove(
          nonRootWithId(root, nonContainmentNonRootIds[1]));
    });
    expectChanges(ChangeType.RemoveNonContainmentEReference);
   expectChangeInTargetModel();
  }

  @Test
  void testReplaceMultiValuedNonContainmentEReference() {
    propagate(getRoot(), root -> {
      EList<NonRoot> multiValuedNonContainmentEReference =
          root.getMultiValuedNonContainmentEReference();
      multiValuedNonContainmentEReference.add(nonRootWithId(root, nonContainmentNonRootIds[0]));
      multiValuedNonContainmentEReference.add(nonRootWithId(root, nonContainmentNonRootIds[1]));
    });
    resetExecutionMonitor();

    propagate(getRoot(), root ->
        root.getMultiValuedNonContainmentEReference().set(
            1, nonRootWithId(root, nonContainmentNonRootIds[2]
            )
        ));

    expectChanges(ChangeType.RemoveNonContainmentEReference, ChangeType.InsertNonContainmentEReference);
    Root from = getRoot();
    assertThat(from.getMultiValuedNonContainmentEReference().size(), is(2));
    assertThat(from.getMultiValuedNonContainmentEReference().get(0).getId(), is(nonContainmentNonRootIds[0]));
    assertThat(from.getMultiValuedNonContainmentEReference().get(1).getId(), is(nonContainmentNonRootIds[2]));

    expectChangeInTargetModel();
  }

  @Test
  @Disabled("Permute operations are not supported by now? No EChange produced")
  void testPermuteMultiValuedNonContainmentEReference() {
    propagate(getRoot(), root -> {
      for (var id : nonContainmentNonRootIds) {
        root.getMultiValuedNonContainmentEReference().add(
            nonRootWithId(root, id)
        );
      }
    });
    resetExecutionMonitor();

    propagate(getRoot(), root -> {
      root.getMultiValuedNonContainmentEReference().sort((a, b) -> -a.getId().compareTo(b.getId()));
    });
    expectChanges(ChangeType.PermuteNonContainmentEReference);
   expectChangeInTargetModel();
  }

  @Test
  void testDeleteEachTestModel() {
    assertThat(resourceAt(SOURCE_MODEL), ModelMatchers.exists());
    assertThat(resourceAt(TARGET_MODEL), ModelMatchers.exists());

    propagate(resourceAt(SOURCE_MODEL), resource -> {
      try {
        resource.delete(null);
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    });
    assertThat(resourceAt(SOURCE_MODEL), ModelMatchers.doesNotExist());
    assertThat(resourceAt(TARGET_MODEL), ModelMatchers.doesNotExist());
  }

  @Test
  void testCreateFurtherModel() {
    propagate(resourceAt(FURTHER_SOURCE_MODEL), resource -> {
      Root root = AllElementTypesCreators.aet.Root();
      root.setId("Further_Source_Test_Model");
      resource.getContents().add(root);
    });
    expectChangeInTargetModel();
  }

  @Test
  void testDeleteFurtherModel() {
    propagate(resourceAt(FURTHER_SOURCE_MODEL), resource -> {
      Root root = AllElementTypesCreators.aet.Root();
      root.setId("Further_Source_Test_Model");
      resource.getContents().add(root);
    });

    assertThat(resourceAt(FURTHER_TARGET_MODEL), ModelMatchers.exists());
    assertThat(resourceAt(FURTHER_SOURCE_MODEL), ModelMatchers.containsModelOf(resourceAt(FURTHER_TARGET_MODEL)));
    propagate(resourceAt(FURTHER_SOURCE_MODEL),
        resource -> {
      try {
        resource.delete(null);
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    });
    assertThat(resourceAt(FURTHER_SOURCE_MODEL), ModelMatchers.doesNotExist());
    assertThat(resourceAt(FURTHER_TARGET_MODEL), ModelMatchers.doesNotExist());
  }
}
