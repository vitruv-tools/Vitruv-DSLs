package tools.vitruv.dsls.reactions.tests.complexTests;

import allElementTypes.NonRoot;
import allElementTypes.NonRootObjectContainerHelper;
import allElementTypes.Root;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.eobject.CreateEObject;
import tools.vitruv.change.atomic.eobject.DeleteEObject;
import tools.vitruv.change.atomic.feature.reference.RemoveEReference;
import tools.vitruv.change.atomic.feature.reference.ReplaceSingleValuedEReference;
import tools.vitruv.change.atomic.root.RemoveRootEObject;
import tools.vitruv.change.composite.description.CompositeContainerChange;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.dsls.reactions.tests.ReactionsExecutionTest;
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.testutils.metamodels.TestMetamodelsPathFactory;

@SuppressWarnings("all")
public class BidirectionalExecutionTests extends ReactionsExecutionTest {
  private static final Path SOURCE_MODEL = TestMetamodelsPathFactory.allElementTypes("BidirectionalSource");

  private static final Path TARGET_MODEL = TestMetamodelsPathFactory.allElementTypes("BidirectionalTarget");

  private String[] nonContainmentNonRootIds = { "NonRootHelper0", "NonRootHelper1", "NonRootHelper2" };

  @Override
  protected TestReactionsCompiler createCompiler(final TestReactionsCompiler.Factory factory) {
    final Consumer<TestReactionsCompiler.TestReactionsCompilerParameters> _function = (TestReactionsCompiler.TestReactionsCompilerParameters it) -> {
      it.setReactions(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("/tools/vitruv/dsls/reactions/tests/AllElementTypesRedundancy.reactions")));
      it.setChangePropagationSegments(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("simpleChangesTests")));
    };
    return factory.createCompiler(_function);
  }

  @BeforeEach
  public void setup() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root _Root = AllElementTypesCreators.aet.Root();
      final Procedure1<Root> _function_1 = (Root it_1) -> {
        it_1.setId("EachTestModelSource");
        NonRootObjectContainerHelper _NonRootObjectContainerHelper = AllElementTypesCreators.aet.NonRootObjectContainerHelper();
        final Procedure1<NonRootObjectContainerHelper> _function_2 = (NonRootObjectContainerHelper it_2) -> {
          it_2.setId("NonRootObjectContainer");
          EList<NonRoot> _nonRootObjectsContainment = it_2.getNonRootObjectsContainment();
          final Function1<String, NonRoot> _function_3 = (String nonRootId) -> {
            NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
            final Procedure1<NonRoot> _function_4 = (NonRoot it_3) -> {
              it_3.setId(nonRootId);
            };
            return ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_4);
          };
          List<NonRoot> _map = ListExtensions.<String, NonRoot>map(((List<String>)Conversions.doWrapArray(this.nonContainmentNonRootIds)), _function_3);
          Iterables.<NonRoot>addAll(_nonRootObjectsContainment, _map);
        };
        NonRootObjectContainerHelper _doubleArrow = ObjectExtensions.<NonRootObjectContainerHelper>operator_doubleArrow(_NonRootObjectContainerHelper, _function_2);
        it_1.setNonRootObjectContainerHelper(_doubleArrow);
      };
      Root _doubleArrow = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(BidirectionalExecutionTests.SOURCE_MODEL), _function);
    MatcherAssert.<Resource>assertThat(this.resourceAt(BidirectionalExecutionTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(BidirectionalExecutionTests.SOURCE_MODEL)));
  }

  private VitruviusChange<EObject> getSourceModelChanges(final PropagatedChange propagatedChange) {
    VitruviusChange<EObject> _consequentialChanges = propagatedChange.getConsequentialChanges();
    final Function1<VitruviusChange<EObject>, Boolean> _function = (VitruviusChange<EObject> it) -> {
      final Function1<URI, Boolean> _function_1 = (URI it_1) -> {
        String _lastSegment = it_1.lastSegment();
        String _string = BidirectionalExecutionTests.SOURCE_MODEL.toString();
        return Boolean.valueOf(Objects.equal(_lastSegment, _string));
      };
      return Boolean.valueOf(IterableExtensions.<URI>exists(it.getChangedURIs(), _function_1));
    };
    return IterableExtensions.<VitruviusChange<EObject>>findFirst(((CompositeContainerChange<EObject>) _consequentialChanges).getChanges(), _function);
  }

  @Test
  public void testBasicBidirectionalApplication() {
    final Consumer<Root> _function = (Root it) -> {
      NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_1 = (NonRoot it_1) -> {
        it_1.setId("bidirectionalId");
      };
      NonRoot _doubleArrow = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_1);
      it.setSingleValuedContainmentEReference(_doubleArrow);
    };
    final List<PropagatedChange> propagatedChanges = this.<Root>propagate(this.<Root>from(Root.class, BidirectionalExecutionTests.TARGET_MODEL), _function);
    MatcherAssert.<Integer>assertThat(Integer.valueOf(propagatedChanges.size()), CoreMatchers.<Integer>is(Integer.valueOf(2)));
    final VitruviusChange<EObject> consequentialSourceModelChange = this.getSourceModelChanges(propagatedChanges.get(0));
    MatcherAssert.<EChange<EObject>>assertThat(consequentialSourceModelChange.getEChanges().get(0), CoreMatchers.<EChange<EObject>>is(CoreMatchers.<EChange<EObject>>instanceOf(CreateEObject.class)));
    MatcherAssert.<EChange<EObject>>assertThat(consequentialSourceModelChange.getEChanges().get(1), CoreMatchers.<EChange<EObject>>is(CoreMatchers.<EChange<EObject>>instanceOf(ReplaceSingleValuedEReference.class)));
    MatcherAssert.<Resource>assertThat(this.resourceAt(BidirectionalExecutionTests.SOURCE_MODEL), ModelMatchers.containsModelOf(this.resourceAt(BidirectionalExecutionTests.TARGET_MODEL)));
    MatcherAssert.<String>assertThat(this.<Root>from(Root.class, BidirectionalExecutionTests.SOURCE_MODEL).getSingleValuedContainmentEReference().getId(), CoreMatchers.<String>is("bidirectionalId"));
    MatcherAssert.<String>assertThat(this.<Root>from(Root.class, BidirectionalExecutionTests.TARGET_MODEL).getSingleValuedContainmentEReference().getId(), CoreMatchers.<String>is("bidirectionalId"));
    MatcherAssert.<List<EChange<EObject>>>assertThat(propagatedChanges.get(1).getConsequentialChanges().getEChanges(), CoreMatchers.<List<EChange<EObject>>>is(CollectionLiterals.<EChange<EObject>>emptyList()));
  }

  @Test
  public void testApplyRemoveInTargetModel() {
    final Consumer<Root> _function = (Root it) -> {
      it.getNonRootObjectContainerHelper().getNonRootObjectsContainment().remove(0);
    };
    final List<PropagatedChange> propagatedChanges = this.<Root>propagate(this.<Root>from(Root.class, BidirectionalExecutionTests.TARGET_MODEL), _function);
    MatcherAssert.<Integer>assertThat(Integer.valueOf(propagatedChanges.size()), CoreMatchers.<Integer>is(Integer.valueOf(2)));
    final VitruviusChange<EObject> consequentialSourceModelChange = this.getSourceModelChanges(propagatedChanges.get(0));
    MatcherAssert.<EChange<EObject>>assertThat(consequentialSourceModelChange.getEChanges().get(0), CoreMatchers.<EChange<EObject>>is(CoreMatchers.<EChange<EObject>>instanceOf(RemoveEReference.class)));
    MatcherAssert.<EChange<EObject>>assertThat(consequentialSourceModelChange.getEChanges().get(1), CoreMatchers.<EChange<EObject>>is(CoreMatchers.<EChange<EObject>>instanceOf(DeleteEObject.class)));
    MatcherAssert.<Resource>assertThat(this.resourceAt(BidirectionalExecutionTests.SOURCE_MODEL), ModelMatchers.containsModelOf(this.resourceAt(BidirectionalExecutionTests.TARGET_MODEL)));
    MatcherAssert.<Integer>assertThat(Integer.valueOf(this.<Root>from(Root.class, BidirectionalExecutionTests.SOURCE_MODEL).getNonRootObjectContainerHelper().getNonRootObjectsContainment().size()), CoreMatchers.<Integer>is(Integer.valueOf(2)));
    MatcherAssert.<Integer>assertThat(Integer.valueOf(this.<Root>from(Root.class, BidirectionalExecutionTests.TARGET_MODEL).getNonRootObjectContainerHelper().getNonRootObjectsContainment().size()), CoreMatchers.<Integer>is(Integer.valueOf(2)));
    MatcherAssert.<List<EChange<EObject>>>assertThat(propagatedChanges.get(1).getConsequentialChanges().getEChanges(), CoreMatchers.<List<EChange<EObject>>>is(CollectionLiterals.<EChange<EObject>>emptyList()));
  }

  @Test
  public void testApplyRemoveRootInTargetModel() {
    final Consumer<Resource> _function = (Resource it) -> {
      try {
        it.delete(CollectionLiterals.<Object, Object>emptyMap());
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    final List<PropagatedChange> propagatedChanges = this.<Resource>propagate(this.resourceAt(BidirectionalExecutionTests.TARGET_MODEL), _function);
    MatcherAssert.<Integer>assertThat(Integer.valueOf(propagatedChanges.size()), CoreMatchers.<Integer>is(Integer.valueOf(2)));
    final VitruviusChange<EObject> consequentialSourceModelChange = this.getSourceModelChanges(propagatedChanges.get(0));
    MatcherAssert.<EChange<EObject>>assertThat(consequentialSourceModelChange.getEChanges().get(0), CoreMatchers.<EChange<EObject>>is(CoreMatchers.<EChange<EObject>>instanceOf(RemoveRootEObject.class)));
    MatcherAssert.<EChange<EObject>>assertThat(consequentialSourceModelChange.getEChanges().get(1), CoreMatchers.<EChange<EObject>>is(CoreMatchers.<EChange<EObject>>instanceOf(DeleteEObject.class)));
    MatcherAssert.<Resource>assertThat(this.resourceAt(BidirectionalExecutionTests.SOURCE_MODEL), ModelMatchers.doesNotExist());
    MatcherAssert.<Resource>assertThat(this.resourceAt(BidirectionalExecutionTests.TARGET_MODEL), ModelMatchers.doesNotExist());
    MatcherAssert.<List<EChange<EObject>>>assertThat(propagatedChanges.get(1).getConsequentialChanges().getEChanges(), CoreMatchers.<List<EChange<EObject>>>is(CollectionLiterals.<EChange<EObject>>emptyList()));
  }
}
