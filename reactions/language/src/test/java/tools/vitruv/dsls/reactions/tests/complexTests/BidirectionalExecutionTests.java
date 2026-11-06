package tools.vitruv.dsls.reactions.tests.complexTests;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import allElementTypes.NonRoot;
import allElementTypes.NonRootObjectContainerHelper;
import allElementTypes.Root;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.atomic.eobject.CreateEObject;
import tools.vitruv.change.atomic.eobject.DeleteEObject;
import tools.vitruv.change.atomic.feature.reference.RemoveEReference;
import tools.vitruv.change.atomic.feature.reference.ReplaceSingleValuedEReference;
import tools.vitruv.change.atomic.root.RemoveRootEObject;
import tools.vitruv.change.composite.description.CompositeContainerChange;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.testutils.matchers.ModelMatchers;
import tools.vitruv.change.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.change.testutils.metamodels.TestMetamodelsPathFactory;
import tools.vitruv.dsls.reactions.tests.ReactionsExecutionTest;
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler;

class BidirectionalExecutionTests extends ReactionsExecutionTest {
  private static final Path SOURCE_MODEL = TestMetamodelsPathFactory.allElementTypes("BidirectionalSource");
  private static final Path TARGET_MODEL = TestMetamodelsPathFactory.allElementTypes("BidirectionalTarget");
  private final String[] nonContainmentNonRootIds = { "NonRootHelper0", "NonRootHelper1", "NonRootHelper2" };

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
  }

  private void expectChangeInTargetModel() {
    assertThat(
        resourceAt(TARGET_MODEL),
        ModelMatchers.containsModelOf(resourceAt(SOURCE_MODEL))
    );
  }

  private VitruviusChange<EObject> getSourceModelChanges(PropagatedChange propagatedChange) {
    CompositeContainerChange<EObject> containerChange =
        (CompositeContainerChange<EObject>) propagatedChange.getConsequentialChanges();
    return containerChange.getChanges().stream()
        .filter(vitruviusChange ->
          vitruviusChange.getChangedURIs().stream().anyMatch(
              uri -> uri.lastSegment().equals(SOURCE_MODEL.toString())
          )
        )
        .findFirst()
        .orElseThrow();
  }

  private void expectChangeAt(VitruviusChange<?> change, int index, Class<?> expectedType) {
    assertThat(change.getEChanges().get(index), instanceOf(expectedType));
  }

  @Test
  void testBasicBidirectionalApplication() {
    List<PropagatedChange> propagatedChanges = propagate(from(Root.class, TARGET_MODEL),
        root -> {
      NonRoot nonRoot = AllElementTypesCreators.aet.NonRoot();
      nonRoot.setId("bidirectionalId");
      root.setSingleValuedContainmentEReference(nonRoot);
    });
    assertThat(propagatedChanges.size(), is(2));
    VitruviusChange<EObject> consequentialSourceModelChange = getSourceModelChanges(propagatedChanges.getFirst());
    expectChangeAt(consequentialSourceModelChange, 0, CreateEObject.class);
    expectChangeAt(consequentialSourceModelChange, 1, ReplaceSingleValuedEReference.class);

    assertThat(resourceAt(SOURCE_MODEL), ModelMatchers.containsModelOf(resourceAt(TARGET_MODEL)));
    assertThat(from(Root.class, SOURCE_MODEL).getSingleValuedContainmentEReference().getId(), is("bidirectionalId"));
    assertThat(from(Root.class, TARGET_MODEL).getSingleValuedContainmentEReference().getId(), is("bidirectionalId"));
    assertThat(propagatedChanges.get(1).getConsequentialChanges().getEChanges(), is(List.of()));
  }

  @Test
  void testApplyRemoveInTargetModel() {
    List<PropagatedChange> propagatedChanges = propagate(from(Root.class, TARGET_MODEL),
        root -> root.getNonRootObjectContainerHelper().getNonRootObjectsContainment().removeFirst());
    assertThat(propagatedChanges.size(), is(2));
    VitruviusChange<EObject> consequentialSourceModelChange = getSourceModelChanges(propagatedChanges.getFirst());

    expectChangeAt(consequentialSourceModelChange, 0, RemoveEReference.class);
    expectChangeAt(consequentialSourceModelChange, 1, DeleteEObject.class);
    assertThat(resourceAt(SOURCE_MODEL), ModelMatchers.containsModelOf(resourceAt(TARGET_MODEL)));
    assertThat(from(Root.class, SOURCE_MODEL).getNonRootObjectContainerHelper().getNonRootObjectsContainment().size(), is(2));
    assertThat(from(Root.class, TARGET_MODEL).getNonRootObjectContainerHelper().getNonRootObjectsContainment().size(), is(2));
    assertThat(propagatedChanges.get(1).getConsequentialChanges().getEChanges(), is(List.of()));
  }

  @Test
  void testApplyRemoveRootInTargetModel() {
    List<PropagatedChange> propagatedChanges = propagate(resourceAt(TARGET_MODEL),
        resource -> {
      try {
        resource.delete(null);
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    });
    assertThat(propagatedChanges.size(), is(2));
    VitruviusChange<EObject> consequentialSourceModelChange = getSourceModelChanges(propagatedChanges.getFirst());
    expectChangeAt(consequentialSourceModelChange, 0, RemoveRootEObject.class);
    expectChangeAt(consequentialSourceModelChange, 1, DeleteEObject.class);
    assertThat(resourceAt(SOURCE_MODEL), ModelMatchers.doesNotExist());
    assertThat(resourceAt(TARGET_MODEL), ModelMatchers.doesNotExist());
    assertThat(propagatedChanges.get(1).getConsequentialChanges().getEChanges(), is(List.of()));
  }
}
