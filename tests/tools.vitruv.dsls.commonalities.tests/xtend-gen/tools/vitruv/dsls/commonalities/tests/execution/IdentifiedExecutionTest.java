package tools.vitruv.dsls.commonalities.tests.execution;

import allElementTypes.NonRoot;
import allElementTypes.Root;
import allElementTypes2.NonRoot2;
import allElementTypes2.Root2;
import com.google.common.collect.Iterables;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.Consumer;
import javax.inject.Inject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import pcm_mockup.Component;
import pcm_mockup.Repository;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.commonalities.tests.CommonalitiesLanguageInjectorProvider;
import tools.vitruv.dsls.commonalities.tests.util.TestCommonalitiesGenerator;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.metamodels.AllElementTypes2Creators;
import tools.vitruv.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.testutils.metamodels.PcmMockupCreators;
import tools.vitruv.testutils.metamodels.TestMetamodelsPathFactory;
import tools.vitruv.testutils.metamodels.UmlMockupCreators;
import uml_mockup.UClass;
import uml_mockup.UPackage;

@ExtendWith(InjectionExtension.class)
@InjectWith(CommonalitiesLanguageInjectorProvider.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("executing simple commonalities")
@SuppressWarnings("all")
public class IdentifiedExecutionTest extends CommonalitiesExecutionTest {
  @Accessors(AccessorType.PROTECTED_GETTER)
  @Inject
  private TestCommonalitiesGenerator generator;

  @BeforeAll
  public void generate(@TestProject(variant = "commonalities") final Path testProject) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes2\" as AllElementTypes2");
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.pcm_mockup\" as PcmMockup");
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.uml_mockup\" as UmlMockup");
    _builder.newLine();
    _builder.newLine();
    _builder.append("concept test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("commonality Identified {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with AllElementTypes:(Root in Resource)");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with AllElementTypes2:(Root2 in Resource)");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with PcmMockup:(Repository in Resource)");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with UmlMockup:(UPackage in Resource)");
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
    _builder.append("= PcmMockup:Repository.name");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= UmlMockup:UPackage.name");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("-> AllElementTypes:Resource.name");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("-> AllElementTypes2:Resource.name");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("-> PcmMockup:Resource.name");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("-> UmlMockup:Resource.name");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("has number {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= AllElementTypes:Root.singleValuedEAttribute");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= AllElementTypes2:Root2.singleValuedEAttribute2");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("has numberList {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= AllElementTypes:Root.multiValuedEAttribute");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= AllElementTypes2:Root2.multiValuedEAttribute2");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("has sub referencing test:SubIdentified {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= AllElementTypes:Root.multiValuedContainmentEReference");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= AllElementTypes2:Root2.multiValuedContainmentEReference2");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= PcmMockup:Repository.components");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= UmlMockup:UPackage.classes");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    Pair<String, CharSequence> _mappedTo = Pair.<String, CharSequence>of("Identified.commonality", _builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
    _builder_1.newLine();
    _builder_1.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes2\" as AllElementTypes2");
    _builder_1.newLine();
    _builder_1.append("import \"http://tools.vitruv.testutils.metamodels.pcm_mockup\" as PcmMockup");
    _builder_1.newLine();
    _builder_1.append("import \"http://tools.vitruv.testutils.metamodels.uml_mockup\" as UmlMockup");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("concept test");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("commonality SubIdentified {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("with AllElementTypes:NonRoot");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("with AllElementTypes2:NonRoot2");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("with PcmMockup:Component");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("with UmlMockup:UClass");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("has name {");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("= AllElementTypes:NonRoot.id");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("= AllElementTypes2:NonRoot2.id2");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("= PcmMockup:Component.name");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("= UmlMockup:UClass.name");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}\t\t\t");
    _builder_1.newLine();
    Pair<String, CharSequence> _mappedTo_1 = Pair.<String, CharSequence>of("SubIdentified.commonality", _builder_1.toString());
    this.generator.generate(testProject, _mappedTo, _mappedTo_1);
  }

  @Override
  protected Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    return this.generator.createChangePropagationSpecifications();
  }

  @Test
  @DisplayName("propagates a root insertion")
  public void rootInsert() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
      final Procedure1<Root2> _function_1 = (Root2 it_1) -> {
        it_1.setId2("testid");
      };
      Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), _function);
    Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_1 = (Root2 it) -> {
      it.setId2("testid");
    };
    Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), ModelMatchers.contains(_doubleArrow));
    Root _Root = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_2 = (Root it) -> {
      it.setId("testid");
    };
    Root _doubleArrow_1 = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("testid")), ModelMatchers.contains(_doubleArrow_1));
    Repository _Repository = PcmMockupCreators.pcm.Repository();
    final Procedure1<Repository> _function_3 = (Repository it) -> {
      it.setName("testid");
    };
    Repository _doubleArrow_2 = ObjectExtensions.<Repository>operator_doubleArrow(_Repository, _function_3);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.pcm_mockup("testid")), 
      ModelMatchers.contains(_doubleArrow_2, ModelMatchers.ignoringFeatures("id")));
    UPackage _Package = UmlMockupCreators.uml.Package();
    final Procedure1<UPackage> _function_4 = (UPackage it) -> {
      it.setName("testid");
    };
    UPackage _doubleArrow_3 = ObjectExtensions.<UPackage>operator_doubleArrow(_Package, _function_4);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.uml_mockup("testid")), 
      ModelMatchers.contains(_doubleArrow_3, ModelMatchers.ignoringFeatures("id")));
  }

  @Test
  @DisplayName("propagates root insertions in multiple resources")
  public void multiRootInsert() {
    final Consumer<String> _function = (String name) -> {
      final Consumer<Resource> _function_1 = (Resource it) -> {
        EList<EObject> _contents = it.getContents();
        Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
        final Procedure1<Root2> _function_2 = (Root2 it_1) -> {
          it_1.setId2(name);
        };
        Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_2);
        _contents.add(_doubleArrow);
      };
      this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2(name)), _function_1);
    };
    Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("first", "second", "third")).forEach(_function);
    Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_1 = (Root2 it) -> {
      it.setId2("first");
    };
    Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("first")), ModelMatchers.contains(_doubleArrow));
    Root _Root = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_2 = (Root it) -> {
      it.setId("first");
    };
    Root _doubleArrow_1 = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("first")), ModelMatchers.contains(_doubleArrow_1));
    Repository _Repository = PcmMockupCreators.pcm.Repository();
    final Procedure1<Repository> _function_3 = (Repository it) -> {
      it.setName("first");
    };
    Repository _doubleArrow_2 = ObjectExtensions.<Repository>operator_doubleArrow(_Repository, _function_3);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.pcm_mockup("first")), 
      ModelMatchers.contains(_doubleArrow_2, ModelMatchers.ignoringFeatures("id")));
    UPackage _Package = UmlMockupCreators.uml.Package();
    final Procedure1<UPackage> _function_4 = (UPackage it) -> {
      it.setName("first");
    };
    UPackage _doubleArrow_3 = ObjectExtensions.<UPackage>operator_doubleArrow(_Package, _function_4);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.uml_mockup("first")), 
      ModelMatchers.contains(_doubleArrow_3, ModelMatchers.ignoringFeatures("id")));
    Root2 _Root2_1 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_5 = (Root2 it) -> {
      it.setId2("second");
    };
    Root2 _doubleArrow_4 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2_1, _function_5);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("second")), ModelMatchers.contains(_doubleArrow_4));
    Root _Root_1 = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_6 = (Root it) -> {
      it.setId("second");
    };
    Root _doubleArrow_5 = ObjectExtensions.<Root>operator_doubleArrow(_Root_1, _function_6);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("second")), ModelMatchers.contains(_doubleArrow_5));
    Repository _Repository_1 = PcmMockupCreators.pcm.Repository();
    final Procedure1<Repository> _function_7 = (Repository it) -> {
      it.setName("second");
    };
    Repository _doubleArrow_6 = ObjectExtensions.<Repository>operator_doubleArrow(_Repository_1, _function_7);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.pcm_mockup("second")), 
      ModelMatchers.contains(_doubleArrow_6, ModelMatchers.ignoringFeatures("id")));
    UPackage _Package_1 = UmlMockupCreators.uml.Package();
    final Procedure1<UPackage> _function_8 = (UPackage it) -> {
      it.setName("second");
    };
    UPackage _doubleArrow_7 = ObjectExtensions.<UPackage>operator_doubleArrow(_Package_1, _function_8);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.uml_mockup("second")), 
      ModelMatchers.contains(_doubleArrow_7, ModelMatchers.ignoringFeatures("id")));
    Root2 _Root2_2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_9 = (Root2 it) -> {
      it.setId2("third");
    };
    Root2 _doubleArrow_8 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2_2, _function_9);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("third")), ModelMatchers.contains(_doubleArrow_8));
    Root _Root_2 = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_10 = (Root it) -> {
      it.setId("third");
    };
    Root _doubleArrow_9 = ObjectExtensions.<Root>operator_doubleArrow(_Root_2, _function_10);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("third")), ModelMatchers.contains(_doubleArrow_9));
    Repository _Repository_2 = PcmMockupCreators.pcm.Repository();
    final Procedure1<Repository> _function_11 = (Repository it) -> {
      it.setName("third");
    };
    Repository _doubleArrow_10 = ObjectExtensions.<Repository>operator_doubleArrow(_Repository_2, _function_11);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.pcm_mockup("third")), 
      ModelMatchers.contains(_doubleArrow_10, ModelMatchers.ignoringFeatures("id")));
    UPackage _Package_2 = UmlMockupCreators.uml.Package();
    final Procedure1<UPackage> _function_12 = (UPackage it) -> {
      it.setName("third");
    };
    UPackage _doubleArrow_11 = ObjectExtensions.<UPackage>operator_doubleArrow(_Package_2, _function_12);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.uml_mockup("third")), 
      ModelMatchers.contains(_doubleArrow_11, ModelMatchers.ignoringFeatures("id")));
  }

  @Test
  @DisplayName("propagates root deletions in multiple resources")
  public void multiRootDelete() {
    final Consumer<String> _function = (String name) -> {
      final Consumer<Resource> _function_1 = (Resource it) -> {
        EList<EObject> _contents = it.getContents();
        Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
        final Procedure1<Root2> _function_2 = (Root2 it_1) -> {
          it_1.setId2(name);
        };
        Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_2);
        _contents.add(_doubleArrow);
      };
      this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2(name)), _function_1);
    };
    Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("first", "second", "third")).forEach(_function);
    final Consumer<Resource> _function_1 = (Resource it) -> {
      it.getContents().clear();
    };
    this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("second")), _function_1);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("second")), ModelMatchers.doesNotExist());
  }

  @Test
  @DisplayName("propagates setting the ID attribute")
  public void setIdAttribute() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
      final Procedure1<Root2> _function_1 = (Root2 it_1) -> {
        it_1.setId2("startid");
      };
      Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("startid")), _function);
    final Consumer<Root2> _function_1 = (Root2 it) -> {
      it.setId2("1st id");
    };
    this.<Root2>propagate(this.<Root2>from(Root2.class, TestMetamodelsPathFactory.allElementTypes2("startid")), _function_1);
    Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_2 = (Root2 it) -> {
      it.setId2("1st id");
    };
    Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("startid")), ModelMatchers.contains(_doubleArrow));
    Root _Root = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_3 = (Root it) -> {
      it.setId("1st id");
    };
    Root _doubleArrow_1 = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_3);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("startid")), ModelMatchers.contains(_doubleArrow_1));
    Repository _Repository = PcmMockupCreators.pcm.Repository();
    final Procedure1<Repository> _function_4 = (Repository it) -> {
      it.setName("1st id");
    };
    Repository _doubleArrow_2 = ObjectExtensions.<Repository>operator_doubleArrow(_Repository, _function_4);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.pcm_mockup("startid")), ModelMatchers.contains(_doubleArrow_2, ModelMatchers.ignoringFeatures("id")));
    UPackage _Package = UmlMockupCreators.uml.Package();
    final Procedure1<UPackage> _function_5 = (UPackage it) -> {
      it.setName("1st id");
    };
    UPackage _doubleArrow_3 = ObjectExtensions.<UPackage>operator_doubleArrow(_Package, _function_5);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.uml_mockup("startid")), ModelMatchers.contains(_doubleArrow_3, ModelMatchers.ignoringFeatures("id")));
    final Consumer<Root> _function_6 = (Root it) -> {
      it.setId("2nd id");
    };
    this.<Root>propagate(this.<Root>from(Root.class, TestMetamodelsPathFactory.allElementTypes("startid")), _function_6);
    Root2 _Root2_1 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_7 = (Root2 it) -> {
      it.setId2("2nd id");
    };
    Root2 _doubleArrow_4 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2_1, _function_7);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("startid")), ModelMatchers.contains(_doubleArrow_4));
    Root _Root_1 = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_8 = (Root it) -> {
      it.setId("2nd id");
    };
    Root _doubleArrow_5 = ObjectExtensions.<Root>operator_doubleArrow(_Root_1, _function_8);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("startid")), ModelMatchers.contains(_doubleArrow_5));
    Repository _Repository_1 = PcmMockupCreators.pcm.Repository();
    final Procedure1<Repository> _function_9 = (Repository it) -> {
      it.setName("2nd id");
    };
    Repository _doubleArrow_6 = ObjectExtensions.<Repository>operator_doubleArrow(_Repository_1, _function_9);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.pcm_mockup("startid")), ModelMatchers.contains(_doubleArrow_6, ModelMatchers.ignoringFeatures("id")));
    UPackage _Package_1 = UmlMockupCreators.uml.Package();
    final Procedure1<UPackage> _function_10 = (UPackage it) -> {
      it.setName("2nd id");
    };
    UPackage _doubleArrow_7 = ObjectExtensions.<UPackage>operator_doubleArrow(_Package_1, _function_10);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.uml_mockup("startid")), ModelMatchers.contains(_doubleArrow_7, ModelMatchers.ignoringFeatures("id")));
    final Consumer<Repository> _function_11 = (Repository it) -> {
      it.setName("3th id");
    };
    this.<Repository>propagate(this.<Repository>from(Repository.class, TestMetamodelsPathFactory.pcm_mockup("startid")), _function_11);
    Root2 _Root2_2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_12 = (Root2 it) -> {
      it.setId2("3th id");
    };
    Root2 _doubleArrow_8 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2_2, _function_12);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("startid")), ModelMatchers.contains(_doubleArrow_8));
    Root _Root_2 = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_13 = (Root it) -> {
      it.setId("3th id");
    };
    Root _doubleArrow_9 = ObjectExtensions.<Root>operator_doubleArrow(_Root_2, _function_13);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("startid")), ModelMatchers.contains(_doubleArrow_9));
    Repository _Repository_2 = PcmMockupCreators.pcm.Repository();
    final Procedure1<Repository> _function_14 = (Repository it) -> {
      it.setName("3th id");
    };
    Repository _doubleArrow_10 = ObjectExtensions.<Repository>operator_doubleArrow(_Repository_2, _function_14);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.pcm_mockup("startid")), ModelMatchers.contains(_doubleArrow_10, ModelMatchers.ignoringFeatures("id")));
    UPackage _Package_2 = UmlMockupCreators.uml.Package();
    final Procedure1<UPackage> _function_15 = (UPackage it) -> {
      it.setName("3th id");
    };
    UPackage _doubleArrow_11 = ObjectExtensions.<UPackage>operator_doubleArrow(_Package_2, _function_15);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.uml_mockup("startid")), ModelMatchers.contains(_doubleArrow_11, ModelMatchers.ignoringFeatures("id")));
    final Consumer<UPackage> _function_16 = (UPackage it) -> {
      it.setName("4th id");
    };
    this.<UPackage>propagate(this.<UPackage>from(UPackage.class, TestMetamodelsPathFactory.uml_mockup("startid")), _function_16);
    Root2 _Root2_3 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_17 = (Root2 it) -> {
      it.setId2("4th id");
    };
    Root2 _doubleArrow_12 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2_3, _function_17);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("startid")), ModelMatchers.contains(_doubleArrow_12));
    Root _Root_3 = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_18 = (Root it) -> {
      it.setId("4th id");
    };
    Root _doubleArrow_13 = ObjectExtensions.<Root>operator_doubleArrow(_Root_3, _function_18);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("startid")), ModelMatchers.contains(_doubleArrow_13));
    Repository _Repository_3 = PcmMockupCreators.pcm.Repository();
    final Procedure1<Repository> _function_19 = (Repository it) -> {
      it.setName("4th id");
    };
    Repository _doubleArrow_14 = ObjectExtensions.<Repository>operator_doubleArrow(_Repository_3, _function_19);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.pcm_mockup("startid")), ModelMatchers.contains(_doubleArrow_14, ModelMatchers.ignoringFeatures("id")));
    UPackage _Package_3 = UmlMockupCreators.uml.Package();
    final Procedure1<UPackage> _function_20 = (UPackage it) -> {
      it.setName("4th id");
    };
    UPackage _doubleArrow_15 = ObjectExtensions.<UPackage>operator_doubleArrow(_Package_3, _function_20);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.uml_mockup("startid")), ModelMatchers.contains(_doubleArrow_15, ModelMatchers.ignoringFeatures("id")));
  }

  @Test
  @DisplayName("propagates setting a simple attribute")
  public void setSimpleAttribute() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
      final Procedure1<Root2> _function_1 = (Root2 it_1) -> {
        it_1.setSingleValuedEAttribute2(Integer.valueOf(0));
        it_1.setId2("test");
      };
      Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("test")), _function);
    final Consumer<Root2> _function_1 = (Root2 it) -> {
      it.setSingleValuedEAttribute2(Integer.valueOf(1));
    };
    this.<Root2>propagate(this.<Root2>from(Root2.class, TestMetamodelsPathFactory.allElementTypes2("test")), _function_1);
    Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_2 = (Root2 it) -> {
      it.setSingleValuedEAttribute2(Integer.valueOf(1));
      it.setId2("test");
    };
    Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("test")), ModelMatchers.contains(_doubleArrow));
    Root _Root = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_3 = (Root it) -> {
      it.setSingleValuedEAttribute(Integer.valueOf(1));
      it.setId("test");
    };
    Root _doubleArrow_1 = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_3);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("test")), ModelMatchers.contains(_doubleArrow_1));
    final Consumer<Root> _function_4 = (Root it) -> {
      it.setSingleValuedEAttribute(Integer.valueOf(2));
    };
    this.<Root>propagate(this.<Root>from(Root.class, TestMetamodelsPathFactory.allElementTypes("test")), _function_4);
    Root2 _Root2_1 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_5 = (Root2 it) -> {
      it.setSingleValuedEAttribute2(Integer.valueOf(2));
      it.setId2("test");
    };
    Root2 _doubleArrow_2 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2_1, _function_5);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("test")), ModelMatchers.contains(_doubleArrow_2));
    Root _Root_1 = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_6 = (Root it) -> {
      it.setSingleValuedEAttribute(Integer.valueOf(2));
      it.setId("test");
    };
    Root _doubleArrow_3 = ObjectExtensions.<Root>operator_doubleArrow(_Root_1, _function_6);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("test")), ModelMatchers.contains(_doubleArrow_3));
    final Consumer<Root2> _function_7 = (Root2 it) -> {
      it.setSingleValuedEAttribute2(Integer.valueOf(3));
    };
    this.<Root2>propagate(this.<Root2>from(Root2.class, TestMetamodelsPathFactory.allElementTypes2("test")), _function_7);
    Root2 _Root2_2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_8 = (Root2 it) -> {
      it.setSingleValuedEAttribute2(Integer.valueOf(3));
      it.setId2("test");
    };
    Root2 _doubleArrow_4 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2_2, _function_8);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("test")), ModelMatchers.contains(_doubleArrow_4));
    Root _Root_2 = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_9 = (Root it) -> {
      it.setSingleValuedEAttribute(Integer.valueOf(3));
      it.setId("test");
    };
    Root _doubleArrow_5 = ObjectExtensions.<Root>operator_doubleArrow(_Root_2, _function_9);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("test")), ModelMatchers.contains(_doubleArrow_5));
    final Consumer<Root> _function_10 = (Root it) -> {
      it.setSingleValuedEAttribute(Integer.valueOf(4));
    };
    this.<Root>propagate(this.<Root>from(Root.class, TestMetamodelsPathFactory.allElementTypes("test")), _function_10);
    Root2 _Root2_3 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_11 = (Root2 it) -> {
      it.setSingleValuedEAttribute2(Integer.valueOf(4));
      it.setId2("test");
    };
    Root2 _doubleArrow_6 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2_3, _function_11);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("test")), ModelMatchers.contains(_doubleArrow_6));
    Root _Root_3 = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_12 = (Root it) -> {
      it.setSingleValuedEAttribute(Integer.valueOf(4));
      it.setId("test");
    };
    Root _doubleArrow_7 = ObjectExtensions.<Root>operator_doubleArrow(_Root_3, _function_12);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("test")), ModelMatchers.contains(_doubleArrow_7));
  }

  @Test
  @DisplayName("propagates setting a multi-valued attribute")
  public void setMultiValue() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
      final Procedure1<Root2> _function_1 = (Root2 it_1) -> {
        EList<Integer> _multiValuedEAttribute2 = it_1.getMultiValuedEAttribute2();
        Iterables.<Integer>addAll(_multiValuedEAttribute2, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
        it_1.setId2("test");
      };
      Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("test")), _function);
    Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_1 = (Root2 it) -> {
      EList<Integer> _multiValuedEAttribute2 = it.getMultiValuedEAttribute2();
      Iterables.<Integer>addAll(_multiValuedEAttribute2, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
      it.setId2("test");
    };
    Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("test")), ModelMatchers.contains(_doubleArrow));
    Root _Root = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_2 = (Root it) -> {
      EList<Integer> _multiValuedEAttribute = it.getMultiValuedEAttribute();
      Iterables.<Integer>addAll(_multiValuedEAttribute, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
      it.setId("test");
    };
    Root _doubleArrow_1 = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("test")), ModelMatchers.contains(_doubleArrow_1));
    final Consumer<Root2> _function_3 = (Root2 it) -> {
      EList<Integer> _multiValuedEAttribute2 = it.getMultiValuedEAttribute2();
      _multiValuedEAttribute2.add(Integer.valueOf(4));
    };
    this.<Root2>propagate(this.<Root2>from(Root2.class, TestMetamodelsPathFactory.allElementTypes2("test")), _function_3);
    Root2 _Root2_1 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_4 = (Root2 it) -> {
      EList<Integer> _multiValuedEAttribute2 = it.getMultiValuedEAttribute2();
      Iterables.<Integer>addAll(_multiValuedEAttribute2, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4))));
      it.setId2("test");
    };
    Root2 _doubleArrow_2 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2_1, _function_4);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("test")), ModelMatchers.contains(_doubleArrow_2));
    Root _Root_1 = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_5 = (Root it) -> {
      EList<Integer> _multiValuedEAttribute = it.getMultiValuedEAttribute();
      Iterables.<Integer>addAll(_multiValuedEAttribute, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4))));
      it.setId("test");
    };
    Root _doubleArrow_3 = ObjectExtensions.<Root>operator_doubleArrow(_Root_1, _function_5);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("test")), ModelMatchers.contains(_doubleArrow_3));
    final Consumer<Root> _function_6 = (Root it) -> {
      EList<Integer> _multiValuedEAttribute = it.getMultiValuedEAttribute();
      _multiValuedEAttribute.add(Integer.valueOf(5));
    };
    this.<Root>propagate(this.<Root>from(Root.class, TestMetamodelsPathFactory.allElementTypes("test")), _function_6);
    Root2 _Root2_2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_7 = (Root2 it) -> {
      EList<Integer> _multiValuedEAttribute2 = it.getMultiValuedEAttribute2();
      Iterables.<Integer>addAll(_multiValuedEAttribute2, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5))));
      it.setId2("test");
    };
    Root2 _doubleArrow_4 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2_2, _function_7);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("test")), ModelMatchers.contains(_doubleArrow_4));
    Root _Root_2 = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_8 = (Root it) -> {
      EList<Integer> _multiValuedEAttribute = it.getMultiValuedEAttribute();
      Iterables.<Integer>addAll(_multiValuedEAttribute, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5))));
      it.setId("test");
    };
    Root _doubleArrow_5 = ObjectExtensions.<Root>operator_doubleArrow(_Root_2, _function_8);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("test")), ModelMatchers.contains(_doubleArrow_5));
  }

  @Test
  @DisplayName("propagates inserting a reference")
  public void rootWithReferenceInsert() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
      final Procedure1<Root2> _function_1 = (Root2 it_1) -> {
        it_1.setId2("testid");
        EList<NonRoot2> _multiValuedContainmentEReference2 = it_1.getMultiValuedContainmentEReference2();
        NonRoot2 _NonRoot2 = AllElementTypes2Creators.aet2.NonRoot2();
        final Procedure1<NonRoot2> _function_2 = (NonRoot2 it_2) -> {
          it_2.setId2("testname");
        };
        NonRoot2 _doubleArrow = ObjectExtensions.<NonRoot2>operator_doubleArrow(_NonRoot2, _function_2);
        _multiValuedContainmentEReference2.add(_doubleArrow);
      };
      Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), _function);
    Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_1 = (Root2 it) -> {
      it.setId2("testid");
      EList<NonRoot2> _multiValuedContainmentEReference2 = it.getMultiValuedContainmentEReference2();
      NonRoot2 _NonRoot2 = AllElementTypes2Creators.aet2.NonRoot2();
      final Procedure1<NonRoot2> _function_2 = (NonRoot2 it_1) -> {
        it_1.setId2("testname");
      };
      NonRoot2 _doubleArrow = ObjectExtensions.<NonRoot2>operator_doubleArrow(_NonRoot2, _function_2);
      _multiValuedContainmentEReference2.add(_doubleArrow);
    };
    Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), ModelMatchers.contains(_doubleArrow));
    Root _Root = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_2 = (Root it) -> {
      it.setId("testid");
      EList<NonRoot> _multiValuedContainmentEReference = it.getMultiValuedContainmentEReference();
      NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_3 = (NonRoot it_1) -> {
        it_1.setId("testname");
      };
      NonRoot _doubleArrow_1 = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_3);
      _multiValuedContainmentEReference.add(_doubleArrow_1);
    };
    Root _doubleArrow_1 = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("testid")), ModelMatchers.contains(_doubleArrow_1));
    Repository _Repository = PcmMockupCreators.pcm.Repository();
    final Procedure1<Repository> _function_3 = (Repository it) -> {
      it.setName("testid");
      EList<Component> _components = it.getComponents();
      Component _Component = PcmMockupCreators.pcm.Component();
      final Procedure1<Component> _function_4 = (Component it_1) -> {
        it_1.setName("testname");
      };
      Component _doubleArrow_2 = ObjectExtensions.<Component>operator_doubleArrow(_Component, _function_4);
      _components.add(_doubleArrow_2);
    };
    Repository _doubleArrow_2 = ObjectExtensions.<Repository>operator_doubleArrow(_Repository, _function_3);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.pcm_mockup("testid")), ModelMatchers.contains(_doubleArrow_2, ModelMatchers.ignoringFeatures("id")));
    UPackage _Package = UmlMockupCreators.uml.Package();
    final Procedure1<UPackage> _function_4 = (UPackage it) -> {
      it.setName("testid");
      EList<UClass> _classes = it.getClasses();
      UClass _Class = UmlMockupCreators.uml.Class();
      final Procedure1<UClass> _function_5 = (UClass it_1) -> {
        it_1.setName("testname");
      };
      UClass _doubleArrow_3 = ObjectExtensions.<UClass>operator_doubleArrow(_Class, _function_5);
      _classes.add(_doubleArrow_3);
    };
    UPackage _doubleArrow_3 = ObjectExtensions.<UPackage>operator_doubleArrow(_Package, _function_4);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.uml_mockup("testid")), ModelMatchers.contains(_doubleArrow_3, ModelMatchers.ignoringFeatures("id")));
  }

  @Test
  @DisplayName("propagates inserting multiple references")
  public void multiReferenceInsert() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
      final Procedure1<Root2> _function_1 = (Root2 it_1) -> {
        it_1.setId2("testid");
        EList<NonRoot2> _multiValuedContainmentEReference2 = it_1.getMultiValuedContainmentEReference2();
        NonRoot2 _NonRoot2 = AllElementTypes2Creators.aet2.NonRoot2();
        final Procedure1<NonRoot2> _function_2 = (NonRoot2 it_2) -> {
          it_2.setId2("first");
        };
        NonRoot2 _doubleArrow = ObjectExtensions.<NonRoot2>operator_doubleArrow(_NonRoot2, _function_2);
        NonRoot2 _NonRoot2_1 = AllElementTypes2Creators.aet2.NonRoot2();
        final Procedure1<NonRoot2> _function_3 = (NonRoot2 it_2) -> {
          it_2.setId2("second");
        };
        NonRoot2 _doubleArrow_1 = ObjectExtensions.<NonRoot2>operator_doubleArrow(_NonRoot2_1, _function_3);
        Iterables.<NonRoot2>addAll(_multiValuedContainmentEReference2, Collections.<NonRoot2>unmodifiableList(CollectionLiterals.<NonRoot2>newArrayList(_doubleArrow, _doubleArrow_1)));
      };
      Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
      _contents.add(_doubleArrow);
    };
    this.<Resource>propagate(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), _function);
    Root2 _Root2 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_1 = (Root2 it) -> {
      it.setId2("testid");
      EList<NonRoot2> _multiValuedContainmentEReference2 = it.getMultiValuedContainmentEReference2();
      NonRoot2 _NonRoot2 = AllElementTypes2Creators.aet2.NonRoot2();
      final Procedure1<NonRoot2> _function_2 = (NonRoot2 it_1) -> {
        it_1.setId2("first");
      };
      NonRoot2 _doubleArrow = ObjectExtensions.<NonRoot2>operator_doubleArrow(_NonRoot2, _function_2);
      NonRoot2 _NonRoot2_1 = AllElementTypes2Creators.aet2.NonRoot2();
      final Procedure1<NonRoot2> _function_3 = (NonRoot2 it_1) -> {
        it_1.setId2("second");
      };
      NonRoot2 _doubleArrow_1 = ObjectExtensions.<NonRoot2>operator_doubleArrow(_NonRoot2_1, _function_3);
      Iterables.<NonRoot2>addAll(_multiValuedContainmentEReference2, Collections.<NonRoot2>unmodifiableList(CollectionLiterals.<NonRoot2>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    Root2 _doubleArrow = ObjectExtensions.<Root2>operator_doubleArrow(_Root2, _function_1);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), ModelMatchers.contains(_doubleArrow));
    Root _Root = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_2 = (Root it) -> {
      it.setId("testid");
      EList<NonRoot> _multiValuedContainmentEReference = it.getMultiValuedContainmentEReference();
      NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_3 = (NonRoot it_1) -> {
        it_1.setId("first");
      };
      NonRoot _doubleArrow_1 = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_3);
      NonRoot _NonRoot_1 = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_4 = (NonRoot it_1) -> {
        it_1.setId("second");
      };
      NonRoot _doubleArrow_2 = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot_1, _function_4);
      Iterables.<NonRoot>addAll(_multiValuedContainmentEReference, Collections.<NonRoot>unmodifiableList(CollectionLiterals.<NonRoot>newArrayList(_doubleArrow_1, _doubleArrow_2)));
    };
    Root _doubleArrow_1 = ObjectExtensions.<Root>operator_doubleArrow(_Root, _function_2);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("testid")), ModelMatchers.contains(_doubleArrow_1));
    Repository _Repository = PcmMockupCreators.pcm.Repository();
    final Procedure1<Repository> _function_3 = (Repository it) -> {
      it.setName("testid");
      EList<Component> _components = it.getComponents();
      Component _Component = PcmMockupCreators.pcm.Component();
      final Procedure1<Component> _function_4 = (Component it_1) -> {
        it_1.setName("first");
      };
      Component _doubleArrow_2 = ObjectExtensions.<Component>operator_doubleArrow(_Component, _function_4);
      Component _Component_1 = PcmMockupCreators.pcm.Component();
      final Procedure1<Component> _function_5 = (Component it_1) -> {
        it_1.setName("second");
      };
      Component _doubleArrow_3 = ObjectExtensions.<Component>operator_doubleArrow(_Component_1, _function_5);
      Iterables.<Component>addAll(_components, Collections.<Component>unmodifiableList(CollectionLiterals.<Component>newArrayList(_doubleArrow_2, _doubleArrow_3)));
    };
    Repository _doubleArrow_2 = ObjectExtensions.<Repository>operator_doubleArrow(_Repository, _function_3);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.pcm_mockup("testid")), ModelMatchers.contains(_doubleArrow_2, ModelMatchers.ignoringFeatures("id")));
    UPackage _Package = UmlMockupCreators.uml.Package();
    final Procedure1<UPackage> _function_4 = (UPackage it) -> {
      it.setName("testid");
      EList<UClass> _classes = it.getClasses();
      UClass _Class = UmlMockupCreators.uml.Class();
      final Procedure1<UClass> _function_5 = (UClass it_1) -> {
        it_1.setName("first");
      };
      UClass _doubleArrow_3 = ObjectExtensions.<UClass>operator_doubleArrow(_Class, _function_5);
      UClass _Class_1 = UmlMockupCreators.uml.Class();
      final Procedure1<UClass> _function_6 = (UClass it_1) -> {
        it_1.setName("second");
      };
      UClass _doubleArrow_4 = ObjectExtensions.<UClass>operator_doubleArrow(_Class_1, _function_6);
      Iterables.<UClass>addAll(_classes, Collections.<UClass>unmodifiableList(CollectionLiterals.<UClass>newArrayList(_doubleArrow_3, _doubleArrow_4)));
    };
    UPackage _doubleArrow_3 = ObjectExtensions.<UPackage>operator_doubleArrow(_Package, _function_4);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.uml_mockup("testid")), ModelMatchers.contains(_doubleArrow_3, ModelMatchers.ignoringFeatures("id")));
    final Consumer<Repository> _function_5 = (Repository it) -> {
      EList<Component> _components = it.getComponents();
      Component _Component = PcmMockupCreators.pcm.Component();
      final Procedure1<Component> _function_6 = (Component it_1) -> {
        it_1.setName("third");
      };
      Component _doubleArrow_4 = ObjectExtensions.<Component>operator_doubleArrow(_Component, _function_6);
      _components.add(_doubleArrow_4);
    };
    this.<Repository>propagate(this.<Repository>from(Repository.class, TestMetamodelsPathFactory.pcm_mockup("testid")), _function_5);
    Root2 _Root2_1 = AllElementTypes2Creators.aet2.Root2();
    final Procedure1<Root2> _function_6 = (Root2 it) -> {
      it.setId2("testid");
      EList<NonRoot2> _multiValuedContainmentEReference2 = it.getMultiValuedContainmentEReference2();
      NonRoot2 _NonRoot2 = AllElementTypes2Creators.aet2.NonRoot2();
      final Procedure1<NonRoot2> _function_7 = (NonRoot2 it_1) -> {
        it_1.setId2("first");
      };
      NonRoot2 _doubleArrow_4 = ObjectExtensions.<NonRoot2>operator_doubleArrow(_NonRoot2, _function_7);
      NonRoot2 _NonRoot2_1 = AllElementTypes2Creators.aet2.NonRoot2();
      final Procedure1<NonRoot2> _function_8 = (NonRoot2 it_1) -> {
        it_1.setId2("second");
      };
      NonRoot2 _doubleArrow_5 = ObjectExtensions.<NonRoot2>operator_doubleArrow(_NonRoot2_1, _function_8);
      NonRoot2 _NonRoot2_2 = AllElementTypes2Creators.aet2.NonRoot2();
      final Procedure1<NonRoot2> _function_9 = (NonRoot2 it_1) -> {
        it_1.setId2("third");
      };
      NonRoot2 _doubleArrow_6 = ObjectExtensions.<NonRoot2>operator_doubleArrow(_NonRoot2_2, _function_9);
      Iterables.<NonRoot2>addAll(_multiValuedContainmentEReference2, Collections.<NonRoot2>unmodifiableList(CollectionLiterals.<NonRoot2>newArrayList(_doubleArrow_4, _doubleArrow_5, _doubleArrow_6)));
    };
    Root2 _doubleArrow_4 = ObjectExtensions.<Root2>operator_doubleArrow(_Root2_1, _function_6);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes2("testid")), ModelMatchers.contains(_doubleArrow_4));
    Root _Root_1 = AllElementTypesCreators.aet.Root();
    final Procedure1<Root> _function_7 = (Root it) -> {
      it.setId("testid");
      EList<NonRoot> _multiValuedContainmentEReference = it.getMultiValuedContainmentEReference();
      NonRoot _NonRoot = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_8 = (NonRoot it_1) -> {
        it_1.setId("first");
      };
      NonRoot _doubleArrow_5 = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot, _function_8);
      NonRoot _NonRoot_1 = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_9 = (NonRoot it_1) -> {
        it_1.setId("second");
      };
      NonRoot _doubleArrow_6 = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot_1, _function_9);
      NonRoot _NonRoot_2 = AllElementTypesCreators.aet.NonRoot();
      final Procedure1<NonRoot> _function_10 = (NonRoot it_1) -> {
        it_1.setId("third");
      };
      NonRoot _doubleArrow_7 = ObjectExtensions.<NonRoot>operator_doubleArrow(_NonRoot_2, _function_10);
      Iterables.<NonRoot>addAll(_multiValuedContainmentEReference, Collections.<NonRoot>unmodifiableList(CollectionLiterals.<NonRoot>newArrayList(_doubleArrow_5, _doubleArrow_6, _doubleArrow_7)));
    };
    Root _doubleArrow_5 = ObjectExtensions.<Root>operator_doubleArrow(_Root_1, _function_7);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.allElementTypes("testid")), ModelMatchers.contains(_doubleArrow_5));
    Repository _Repository_1 = PcmMockupCreators.pcm.Repository();
    final Procedure1<Repository> _function_8 = (Repository it) -> {
      it.setName("testid");
      EList<Component> _components = it.getComponents();
      Component _Component = PcmMockupCreators.pcm.Component();
      final Procedure1<Component> _function_9 = (Component it_1) -> {
        it_1.setName("first");
      };
      Component _doubleArrow_6 = ObjectExtensions.<Component>operator_doubleArrow(_Component, _function_9);
      Component _Component_1 = PcmMockupCreators.pcm.Component();
      final Procedure1<Component> _function_10 = (Component it_1) -> {
        it_1.setName("second");
      };
      Component _doubleArrow_7 = ObjectExtensions.<Component>operator_doubleArrow(_Component_1, _function_10);
      Component _Component_2 = PcmMockupCreators.pcm.Component();
      final Procedure1<Component> _function_11 = (Component it_1) -> {
        it_1.setName("third");
      };
      Component _doubleArrow_8 = ObjectExtensions.<Component>operator_doubleArrow(_Component_2, _function_11);
      Iterables.<Component>addAll(_components, Collections.<Component>unmodifiableList(CollectionLiterals.<Component>newArrayList(_doubleArrow_6, _doubleArrow_7, _doubleArrow_8)));
    };
    Repository _doubleArrow_6 = ObjectExtensions.<Repository>operator_doubleArrow(_Repository_1, _function_8);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.pcm_mockup("testid")), ModelMatchers.contains(_doubleArrow_6, ModelMatchers.ignoringFeatures("id")));
    UPackage _Package_1 = UmlMockupCreators.uml.Package();
    final Procedure1<UPackage> _function_9 = (UPackage it) -> {
      it.setName("testid");
      EList<UClass> _classes = it.getClasses();
      UClass _Class = UmlMockupCreators.uml.Class();
      final Procedure1<UClass> _function_10 = (UClass it_1) -> {
        it_1.setName("first");
      };
      UClass _doubleArrow_7 = ObjectExtensions.<UClass>operator_doubleArrow(_Class, _function_10);
      UClass _Class_1 = UmlMockupCreators.uml.Class();
      final Procedure1<UClass> _function_11 = (UClass it_1) -> {
        it_1.setName("second");
      };
      UClass _doubleArrow_8 = ObjectExtensions.<UClass>operator_doubleArrow(_Class_1, _function_11);
      UClass _Class_2 = UmlMockupCreators.uml.Class();
      final Procedure1<UClass> _function_12 = (UClass it_1) -> {
        it_1.setName("third");
      };
      UClass _doubleArrow_9 = ObjectExtensions.<UClass>operator_doubleArrow(_Class_2, _function_12);
      Iterables.<UClass>addAll(_classes, Collections.<UClass>unmodifiableList(CollectionLiterals.<UClass>newArrayList(_doubleArrow_7, _doubleArrow_8, _doubleArrow_9)));
    };
    UPackage _doubleArrow_7 = ObjectExtensions.<UPackage>operator_doubleArrow(_Package_1, _function_9);
    MatcherAssert.<Resource>assertThat(this.resourceAt(TestMetamodelsPathFactory.uml_mockup("testid")), ModelMatchers.contains(_doubleArrow_7, ModelMatchers.ignoringFeatures("id")));
  }

  @Pure
  protected TestCommonalitiesGenerator getGenerator() {
    return this.generator;
  }
}
