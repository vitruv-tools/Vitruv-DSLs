package tools.vitruv.dsls.reactions.tests.simpleChangesTests;

import allElementTypes.NonRoot;
import allElementTypes.NonRootObjectContainerHelper;
import allElementTypes.Root;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.reactions.tests.ExecutionMonitor;
import tools.vitruv.dsls.reactions.tests.ReactionsExecutionTest;
import tools.vitruv.dsls.reactions.tests.TestReactionsCompiler;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.testutils.metamodels.TestMetamodelsPathFactory;

@SuppressWarnings("all")
public class SimpleChangesTests extends ReactionsExecutionTest {
  private static final Path SOURCE_MODEL = TestMetamodelsPathFactory.allElementTypes("SimpleChangeSource");

  private static final Path TARGET_MODEL = TestMetamodelsPathFactory.allElementTypes("SimpleChangeTarget");

  private static final Path FURTHER_SOURCE_MODEL = TestMetamodelsPathFactory.allElementTypes("FurtherSource");

  private static final Path FURTHER_TARGET_MODEL = TestMetamodelsPathFactory.allElementTypes("FurtherTarget");

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
  public void createRoot() {
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
    this.<Resource>propagate(this.resourceAt(SimpleChangesTests.SOURCE_MODEL), _function);
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
    SimpleChangesTests.getExecutionMonitor().reset();
  }

  private static SimpleChangesTestsExecutionMonitor getExecutionMonitor() {
    return SimpleChangesTestsExecutionMonitor.instance;
  }

  private NonRoot nonRootWithId(final Root rootObject, final String searchId) {
    final Function1<NonRoot, Boolean> _function = (NonRoot it) -> {
      String _id = it.getId();
      return Boolean.valueOf(Objects.equal(_id, searchId));
    };
    return IterableExtensions.<NonRoot>findFirst(rootObject.getNonRootObjectContainerHelper().getNonRootObjectsContainment(), _function);
  }

  @Test
  @Disabled("Unset does not produce any change event at the moment")
  public void testUnsetSingleValuedEAttribute() {
    final Consumer<Root> _function = (Root it) -> {
      it.setSingleValuedEAttribute(null);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.UnsetEAttribute));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  @Disabled
  public void testUnsetSingleValuedNonContainmentEReference() {
    final Consumer<Root> _function = (Root it) -> {
      it.setSingleValuedNonContainmentEReference(this.nonRootWithId(it, this.nonContainmentNonRootIds[1]));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    SimpleChangesTests.getExecutionMonitor().reset();
    final Consumer<Root> _function_1 = (Root it) -> {
      it.setSingleValuedNonContainmentEReference(null);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_1);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.UnsetNonContainmentEReference));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testUpdateSingleValuedEAttribute() {
    final Consumer<Root> _function = (Root it) -> {
      it.setSingleValuedEAttribute(Integer.valueOf((-1)));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.UpdateSingleValuedEAttribute));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testUpdateSingleValuedPrimitiveTypeEAttribute() {
    final Consumer<Root> _function = (Root it) -> {
      it.setSingleValuedPrimitiveTypeEAttribute((-1));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.UpdateSingleValuedPrimitveTypeEAttribute));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testCreateSingleValuedContainmentEReference() {
    final Consumer<Root> _function = (Root it) -> {
      NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_1 = (NonRoot it_1) -> {
        it_1.setId("singleValuedContainmentNonRootTest");
      };
      NonRoot _doubleArrow = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_1);
      it.setSingleValuedContainmentEReference(_doubleArrow);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.CreateEObject, SimpleChangesTestsExecutionMonitor.ChangeType.CreateNonRootEObjectSingle));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testDeleteSingleValuedContainmentEReference() {
    NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
    final Procedure1<NonRoot> _function = (NonRoot it) -> {
      it.setId("singleValuedContainmentNonRoot");
    };
    final NonRoot oldElement = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function);
    final Consumer<Root> _function_1 = (Root it) -> {
      it.setSingleValuedContainmentEReference(oldElement);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_1);
    SimpleChangesTests.getExecutionMonitor().reset();
    final Consumer<Root> _function_2 = (Root it) -> {
      it.setSingleValuedContainmentEReference(null);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_2);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.DeleteEObject, SimpleChangesTestsExecutionMonitor.ChangeType.DeleteNonRootEObjectSingle));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testReplaceSingleValuedContainmentEReference() {
    final Consumer<Root> _function = (Root it) -> {
      NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_1 = (NonRoot it_1) -> {
        it_1.setId("singleValuedContainmentNonRootBefore");
      };
      NonRoot _doubleArrow = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_1);
      it.setSingleValuedContainmentEReference(_doubleArrow);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    SimpleChangesTests.getExecutionMonitor().reset();
    final Consumer<Root> _function_1 = (Root it) -> {
      NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_2 = (NonRoot it_1) -> {
        it_1.setId("singleValuedContainmentNonRootAfter");
      };
      NonRoot _doubleArrow = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_2);
      it.setSingleValuedContainmentEReference(_doubleArrow);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_1);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), 
      ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.DeleteNonRootEObjectSingle, SimpleChangesTestsExecutionMonitor.ChangeType.DeleteEObject, SimpleChangesTestsExecutionMonitor.ChangeType.CreateNonRootEObjectSingle, SimpleChangesTestsExecutionMonitor.ChangeType.CreateEObject));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testSetSingleValuedNonContainmentEReference() {
    final String testId = this.nonContainmentNonRootIds[1];
    final Consumer<Root> _function = (Root it) -> {
      it.setSingleValuedNonContainmentEReference(this.nonRootWithId(it, testId));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.UpdateSingleValuedNonContainmentEReference));
    MatcherAssert.<String>assertThat(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL).getSingleValuedNonContainmentEReference().getId(), CoreMatchers.<String>is(testId));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testReplaceSingleValuedNonContainmentEReference() {
    final Consumer<Root> _function = (Root it) -> {
      it.setSingleValuedNonContainmentEReference(this.nonRootWithId(it, this.nonContainmentNonRootIds[0]));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    SimpleChangesTests.getExecutionMonitor().reset();
    final String testId = this.nonContainmentNonRootIds[1];
    final Consumer<Root> _function_1 = (Root it) -> {
      it.setSingleValuedNonContainmentEReference(this.nonRootWithId(it, testId));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_1);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.UpdateSingleValuedNonContainmentEReference));
    MatcherAssert.<String>assertThat(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL).getSingleValuedNonContainmentEReference().getId(), CoreMatchers.<String>is(testId));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testAddMultiValuedEAttribute() {
    final Consumer<Root> _function = (Root it) -> {
      EList<Integer> _multiValuedEAttribute = it.getMultiValuedEAttribute();
      _multiValuedEAttribute.add(Integer.valueOf(1));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.InsertEAttributeValue));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testDeleteMultiValuedEAttribute() {
    final Consumer<Root> _function = (Root it) -> {
      EList<Integer> _multiValuedEAttribute = it.getMultiValuedEAttribute();
      Iterables.<Integer>addAll(_multiValuedEAttribute, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2))));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    SimpleChangesTests.getExecutionMonitor().reset();
    final Consumer<Root> _function_1 = (Root it) -> {
      EList<Integer> _multiValuedEAttribute = it.getMultiValuedEAttribute();
      _multiValuedEAttribute.remove(Integer.valueOf(1));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_1);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.RemoveEAttributeValue));
    MatcherAssert.<Integer>assertThat(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL).getMultiValuedEAttribute().get(0), CoreMatchers.<Integer>is(Integer.valueOf(2)));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testReplaceMultiValuedEAttribute() {
    final Consumer<Root> _function = (Root it) -> {
      EList<Integer> _multiValuedEAttribute = it.getMultiValuedEAttribute();
      Iterables.<Integer>addAll(_multiValuedEAttribute, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2))));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    SimpleChangesTests.getExecutionMonitor().reset();
    final Consumer<Root> _function_1 = (Root it) -> {
      it.getMultiValuedEAttribute().set(1, Integer.valueOf(3));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_1);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.RemoveEAttributeValue, SimpleChangesTestsExecutionMonitor.ChangeType.InsertEAttributeValue));
    MatcherAssert.<Integer>assertThat(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL).getMultiValuedEAttribute().get(1), CoreMatchers.<Integer>is(Integer.valueOf(3)));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testAddMultiValuedContainmentEReference() {
    final Consumer<Root> _function = (Root it) -> {
      EList<NonRoot> _multiValuedContainmentEReference = it.getMultiValuedContainmentEReference();
      NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_1 = (NonRoot it_1) -> {
        it_1.setId("multiValuedContainmentNonRootTest");
      };
      NonRoot _doubleArrow = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_1);
      _multiValuedContainmentEReference.add(_doubleArrow);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.CreateNonRootEObjectInList, SimpleChangesTestsExecutionMonitor.ChangeType.CreateEObject));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testDeleteMultiValuedContainmentEReference() {
    final Consumer<Root> _function = (Root it) -> {
      EList<NonRoot> _multiValuedContainmentEReference = it.getMultiValuedContainmentEReference();
      NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_1 = (NonRoot it_1) -> {
        it_1.setId("multiValuedContainmentNonRootTest");
      };
      NonRoot _doubleArrow = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_1);
      _multiValuedContainmentEReference.add(_doubleArrow);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    SimpleChangesTests.getExecutionMonitor().reset();
    final Consumer<Root> _function_1 = (Root it) -> {
      final Predicate<NonRoot> _function_2 = (NonRoot it_1) -> {
        String _id = it_1.getId();
        return Objects.equal(_id, "multiValuedContainmentNonRootTest");
      };
      it.getMultiValuedContainmentEReference().removeIf(_function_2);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_1);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.DeleteNonRootEObjectInList, SimpleChangesTestsExecutionMonitor.ChangeType.DeleteEObject));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testReplaceMultiValuedContainmentEReference() {
    final Consumer<Root> _function = (Root it) -> {
      EList<NonRoot> _multiValuedContainmentEReference = it.getMultiValuedContainmentEReference();
      NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_1 = (NonRoot it_1) -> {
        it_1.setId("multiValuedContainmentNonRootBefore");
      };
      NonRoot _doubleArrow = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_1);
      _multiValuedContainmentEReference.add(_doubleArrow);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    SimpleChangesTests.getExecutionMonitor().reset();
    final Consumer<Root> _function_1 = (Root it) -> {
      EList<NonRoot> _multiValuedContainmentEReference = it.getMultiValuedContainmentEReference();
      NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_2 = (NonRoot it_1) -> {
        it_1.setId("multiValuedContainmentNonRootAfter");
      };
      NonRoot _doubleArrow = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_2);
      _multiValuedContainmentEReference.set(0, _doubleArrow);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_1);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), 
      ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.DeleteNonRootEObjectInList, SimpleChangesTestsExecutionMonitor.ChangeType.DeleteEObject, SimpleChangesTestsExecutionMonitor.ChangeType.CreateNonRootEObjectInList, SimpleChangesTestsExecutionMonitor.ChangeType.CreateEObject));
    MatcherAssert.<String>assertThat(IterableExtensions.<NonRoot>last(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL).getMultiValuedContainmentEReference()).getId(), 
      CoreMatchers.<String>is("multiValuedContainmentNonRootAfter"));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testInsertMultiValuedNonContainmentEReference() {
    final Consumer<Root> _function = (Root it) -> {
      EList<NonRoot> _multiValuedNonContainmentEReference = it.getMultiValuedNonContainmentEReference();
      NonRoot _nonRootWithId = this.nonRootWithId(it, this.nonContainmentNonRootIds[0]);
      _multiValuedNonContainmentEReference.add(_nonRootWithId);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.InsertNonContainmentEReference));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testRemoveMultiValuedNonContainmentEReference() {
    final Consumer<Root> _function = (Root it) -> {
      EList<NonRoot> _multiValuedNonContainmentEReference = it.getMultiValuedNonContainmentEReference();
      NonRoot _nonRootWithId = this.nonRootWithId(it, this.nonContainmentNonRootIds[1]);
      _multiValuedNonContainmentEReference.add(_nonRootWithId);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    SimpleChangesTests.getExecutionMonitor().reset();
    final Consumer<Root> _function_1 = (Root it) -> {
      EList<NonRoot> _multiValuedNonContainmentEReference = it.getMultiValuedNonContainmentEReference();
      NonRoot _nonRootWithId = this.nonRootWithId(it, this.nonContainmentNonRootIds[1]);
      _multiValuedNonContainmentEReference.remove(_nonRootWithId);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_1);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.RemoveNonContainmentEReference));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testReplaceMultiValuedNonContainmentEReference() {
    final Consumer<Root> _function = (Root it) -> {
      EList<NonRoot> _multiValuedNonContainmentEReference = it.getMultiValuedNonContainmentEReference();
      NonRoot _nonRootWithId = this.nonRootWithId(it, this.nonContainmentNonRootIds[0]);
      _multiValuedNonContainmentEReference.add(_nonRootWithId);
      EList<NonRoot> _multiValuedNonContainmentEReference_1 = it.getMultiValuedNonContainmentEReference();
      NonRoot _nonRootWithId_1 = this.nonRootWithId(it, this.nonContainmentNonRootIds[1]);
      _multiValuedNonContainmentEReference_1.add(_nonRootWithId_1);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    SimpleChangesTests.getExecutionMonitor().reset();
    final Consumer<Root> _function_1 = (Root it) -> {
      it.getMultiValuedNonContainmentEReference().set(1, this.nonRootWithId(it, this.nonContainmentNonRootIds[2]));
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_1);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.RemoveNonContainmentEReference, SimpleChangesTestsExecutionMonitor.ChangeType.InsertNonContainmentEReference));
    Root _from = this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL);
    final Procedure1<Root> _function_2 = (Root it) -> {
      MatcherAssert.<Integer>assertThat(Integer.valueOf(it.getMultiValuedNonContainmentEReference().size()), CoreMatchers.<Integer>is(Integer.valueOf(2)));
      MatcherAssert.<String>assertThat(it.getMultiValuedNonContainmentEReference().get(0).getId(), CoreMatchers.<String>is(this.nonContainmentNonRootIds[0]));
      MatcherAssert.<String>assertThat(it.getMultiValuedNonContainmentEReference().get(1).getId(), CoreMatchers.<String>is(this.nonContainmentNonRootIds[2]));
    };
    ObjectExtensions.<Root>operator_doubleArrow(_from, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  @Disabled("Permute operations are not supported by now? No EChange produced")
  public void testPermuteMultiValuedNonContainmentEReference() {
    final Consumer<Root> _function = (Root it) -> {
      EList<NonRoot> _multiValuedNonContainmentEReference = it.getMultiValuedNonContainmentEReference();
      final Function1<String, NonRoot> _function_1 = (String id) -> {
        return this.nonRootWithId(it, id);
      };
      List<NonRoot> _map = ListExtensions.<String, NonRoot>map(((List<String>)Conversions.doWrapArray(this.nonContainmentNonRootIds)), _function_1);
      Iterables.<NonRoot>addAll(_multiValuedNonContainmentEReference, _map);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function);
    SimpleChangesTests.getExecutionMonitor().reset();
    final Consumer<Root> _function_1 = (Root it) -> {
      final Comparator<NonRoot> _function_2 = (NonRoot a, NonRoot b) -> {
        int _compareTo = a.getId().compareTo(b.getId());
        return (-_compareTo);
      };
      ECollections.<NonRoot>sort(it.getMultiValuedNonContainmentEReference(), _function_2);
    };
    this.<Root>propagate(this.<Root>from(Root.class, SimpleChangesTests.SOURCE_MODEL), _function_1);
    MatcherAssert.<SimpleChangesTestsExecutionMonitor>assertThat(SimpleChangesTests.getExecutionMonitor(), ExecutionMonitor.<SimpleChangesTestsExecutionMonitor.ChangeType>observedExecutions(SimpleChangesTestsExecutionMonitor.ChangeType.PermuteNonContainmentEReference));
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.SOURCE_MODEL)));
  }

  @Test
  public void testDeleteEachTestModel() {
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.SOURCE_MODEL), ModelMatchers.exists());
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.exists());
    final Consumer<Resource> _function = (Resource it) -> {
      try {
        it.delete(CollectionLiterals.<Object, Object>emptyMap());
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    this.<Resource>propagate(this.resourceAt(SimpleChangesTests.SOURCE_MODEL), _function);
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.SOURCE_MODEL), ModelMatchers.doesNotExist());
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.TARGET_MODEL), ModelMatchers.doesNotExist());
  }

  @Test
  public void testCreateFurtherModel() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root _Root = AllElementTypesCreators.aet.Root();
      final Procedure1<Root> _function_1 = (Root it_1) -> {
        it_1.setId("Further_Source_Test_Model");
      };
      Root _doubleArrow = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(SimpleChangesTests.FURTHER_SOURCE_MODEL), _function);
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.FURTHER_TARGET_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.FURTHER_SOURCE_MODEL)));
  }

  @Test
  public void testDeleteFurtherModel() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root _Root = AllElementTypesCreators.aet.Root();
      final Procedure1<Root> _function_1 = (Root it_1) -> {
        it_1.setId("Further_Source_Test_Model");
      };
      Root _doubleArrow = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(SimpleChangesTests.FURTHER_SOURCE_MODEL), _function);
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.FURTHER_TARGET_MODEL), ModelMatchers.exists());
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.FURTHER_SOURCE_MODEL), ModelMatchers.containsModelOf(this.resourceAt(SimpleChangesTests.FURTHER_TARGET_MODEL)));
    final Consumer<Resource> _function_1 = (Resource it) -> {
      try {
        it.delete(CollectionLiterals.<Object, Object>emptyMap());
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    this.<Resource>propagate(this.resourceAt(SimpleChangesTests.FURTHER_SOURCE_MODEL), _function_1);
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.FURTHER_SOURCE_MODEL), ModelMatchers.doesNotExist());
    MatcherAssert.<Resource>assertThat(this.resourceAt(SimpleChangesTests.FURTHER_TARGET_MODEL), ModelMatchers.doesNotExist());
  }
}
