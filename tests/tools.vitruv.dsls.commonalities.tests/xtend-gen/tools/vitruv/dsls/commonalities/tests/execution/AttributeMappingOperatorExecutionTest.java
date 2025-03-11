package tools.vitruv.dsls.commonalities.tests.execution;

import allElementTypes.Root;
import allElementTypes2.Root2;
import com.google.common.collect.Iterables;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.Consumer;
import javax.inject.Inject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.commonalities.tests.CommonalitiesLanguageInjectorProvider;
import tools.vitruv.dsls.commonalities.tests.util.TestCommonalitiesGenerator;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.metamodels.AllElementTypes2Creators;
import tools.vitruv.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.testutils.metamodels.TestMetamodelsPathFactory;

@ExtendWith(InjectionExtension.class)
@InjectWith(CommonalitiesLanguageInjectorProvider.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("executing a commonality with attribute mapping operators")
@SuppressWarnings("all")
public class AttributeMappingOperatorExecutionTest extends CommonalitiesExecutionTest {
  @Inject
  private TestCommonalitiesGenerator generator;

  @BeforeAll
  public void generate(@TestProject(variant = "commonalities") final Path testProject) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import tools.vitruv.dsls.commonalities.tests.operators.multiply");
    _builder.newLine();
    _builder.append("import tools.vitruv.dsls.commonalities.tests.operators.digits");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes2\" as AllElementTypes2");
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.pcm_mockup\" as PcmMockup");
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.uml_mockup\" as UmlMockup");
    _builder.newLine();
    _builder.newLine();
    _builder.append("concept operators");
    _builder.newLine();
    _builder.newLine();
    _builder.append("commonality WithAttributeMappingOperators {");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with AllElementTypes:(Root in Resource)");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with AllElementTypes2:(Root2 in Resource)");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("has id {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= AllElementTypes:Root.id");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= AllElementTypes2:Root2.id2");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("-> AllElementTypes:Resource.name");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("-> AllElementTypes2:Resource.name");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("has number {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("// number <-> number mapping:");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= multiply(AllElementTypes:Root.singleValuedPrimitiveTypeEAttribute, 1000)");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= AllElementTypes2:Root2.singleValuedPrimitiveTypeEAttribute2");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("has numberList {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("// number <-> numberList mapping:");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= digits(AllElementTypes:Root.singleValuedEAttribute)");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= AllElementTypes2:Root2.multiValuedEAttribute2");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    Pair<String, CharSequence> _mappedTo = Pair.<String, CharSequence>of("WithAttributeMappingOperators.commonality", _builder.toString());
    this.generator.generate(testProject, _mappedTo);
  }

  @Override
  protected Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    return this.generator.createChangePropagationSpecifications();
  }

  @Test
  @DisplayName("maps a simple attribute")
  public void singleToSingleValuedAttribute() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root _Root = AllElementTypesCreators.aet.Root();
      final Procedure1<Root> _function_1 = (Root it_1) -> {
        it_1.setId("testid");
        it_1.setSingleValuedPrimitiveTypeEAttribute(123);
      };
      Root _doubleArrow = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("testid")), _function);
    Root _Root = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_1 = (Root it) -> {
      it.setId("testid");
      it.setSingleValuedPrimitiveTypeEAttribute(123);
    };
    Root _doubleArrow = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_1);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("testid")), ModelMatchers.contains(_doubleArrow));
    Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_2 = (Root2 it) -> {
      it.setId2("testid");
      it.setSingleValuedPrimitiveTypeEAttribute2(123000);
    };
    Root2 _doubleArrow_1 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), ModelMatchers.contains(_doubleArrow_1, ModelMatchers.ignoringFeatures("multiValuedEAttribute2")));
  }

  @Test
  @DisplayName("maps a simple attribute (reverse)")
  public void singleToSingleValuedAttributeReverse() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
      final Procedure1<Root2> _function_1 = (Root2 it_1) -> {
        it_1.setId2("testid");
        it_1.setSingleValuedPrimitiveTypeEAttribute2(123500);
      };
      Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), _function);
    Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_1 = (Root2 it) -> {
      it.setId2("testid");
      it.setSingleValuedPrimitiveTypeEAttribute2(123000);
    };
    Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), ModelMatchers.contains(_doubleArrow));
    Root _Root = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_2 = (Root it) -> {
      it.setId("testid");
      it.setSingleValuedPrimitiveTypeEAttribute(123);
    };
    Root _doubleArrow_1 = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("testid")), ModelMatchers.contains(_doubleArrow_1));
  }

  @Test
  @DisplayName("maps a single-valued attribute to a multi-valued one")
  public void singleToMultiValuedAttribute() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root _Root = AllElementTypesCreators.aet.Root();
      final Procedure1<Root> _function_1 = (Root it_1) -> {
        it_1.setId("testid");
        it_1.setSingleValuedEAttribute(Integer.valueOf(324));
      };
      Root _doubleArrow = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("testid")), _function);
    Root _Root = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_1 = (Root it) -> {
      it.setId("testid");
      it.setSingleValuedEAttribute(Integer.valueOf(324));
    };
    Root _doubleArrow = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_1);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("testid")), ModelMatchers.contains(_doubleArrow));
    Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_2 = (Root2 it) -> {
      it.setId2("testid");
      EList<Integer> _multiValuedEAttribute2 = it.getMultiValuedEAttribute2();
      Iterables.<Integer>addAll(_multiValuedEAttribute2, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(4))));
    };
    Root2 _doubleArrow_1 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), ModelMatchers.contains(_doubleArrow_1));
  }

  @Test
  @DisplayName("maps a multi-valued attribute to single-valued one")
  public void multiToSingleValuedAttribute() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
      final Procedure1<Root2> _function_1 = (Root2 it_1) -> {
        it_1.setId2("testid");
        EList<Integer> _multiValuedEAttribute2 = it_1.getMultiValuedEAttribute2();
        Iterables.<Integer>addAll(_multiValuedEAttribute2, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(4))));
      };
      Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), _function);
    Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_1 = (Root2 it) -> {
      it.setId2("testid");
      EList<Integer> _multiValuedEAttribute2 = it.getMultiValuedEAttribute2();
      Iterables.<Integer>addAll(_multiValuedEAttribute2, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(4))));
    };
    Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), ModelMatchers.contains(_doubleArrow));
    Root _Root = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_2 = (Root it) -> {
      it.setId("testid");
      it.setSingleValuedEAttribute(Integer.valueOf(324));
    };
    Root _doubleArrow_1 = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("testid")), ModelMatchers.contains(_doubleArrow_1));
  }
}
