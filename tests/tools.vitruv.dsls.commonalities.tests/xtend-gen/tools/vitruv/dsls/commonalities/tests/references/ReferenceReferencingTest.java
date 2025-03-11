package tools.vitruv.dsls.commonalities.tests.references;

import javax.inject.Inject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.tests.CommonalitiesLanguageInjectorProvider;
import tools.vitruv.dsls.commonalities.tests.util.CommonalitiesLanguageCreators;
import tools.vitruv.dsls.commonalities.tests.util.CommonalitiesLanguageElementsPrinter;
import tools.vitruv.dsls.commonalities.tests.util.CommonalityParseHelper;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.printing.ModelPrinterChange;
import tools.vitruv.testutils.printing.UseModelPrinter;

@ExtendWith({ InjectionExtension.class, ModelPrinterChange.class })
@InjectWith(CommonalitiesLanguageInjectorProvider.class)
@UseModelPrinter(CommonalitiesLanguageElementsPrinter.class)
@DisplayName("referencing Commonalities in References")
@SuppressWarnings("all")
public class ReferenceReferencingTest {
  @Inject
  @Extension
  private CommonalityParseHelper _commonalityParseHelper;

  @Test
  @DisplayName("resolves a reference to the declaring Commonality")
  public void selfReference() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
    _builder.newLine();
    _builder.newLine();
    _builder.append("concept test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("commonality Test {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with AllElementTypes:Root");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("has selfref referencing test:Test {}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final Commonality commonality = this._commonalityParseHelper.parseAndValidate(_builder).getCommonality();
    CommonalityReference _CommonalityReference = CommonalitiesLanguageCreators.commonalities.CommonalityReference();
    final Procedure1<CommonalityReference> _function = (CommonalityReference it) -> {
      it.setName("selfref");
      it.setReferenceType(commonality);
    };
    CommonalityReference _doubleArrow = ObjectExtensions.<CommonalityReference>operator_doubleArrow(_CommonalityReference, _function);
    MatcherAssert.<CommonalityReference>assertThat(commonality.getReferences().get(0), ModelMatchers.<CommonalityReference>equalsDeeply(_doubleArrow, ModelMatchers.usingEqualsForReferencesTo(CommonalitiesLanguageCreators.commonalities.Commonality().eClass())));
  }

  @Test
  @DisplayName("resolves a reference to another Commonality")
  public void foreignReference() {
    final Procedure1<ResourceSet> _function = (ResourceSet it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("concept Referenced");
      _builder.newLine();
      _builder.newLine();
      _builder.append("commonality Target {}");
      _builder.newLine();
      final CommonalityFile referenced = this._commonalityParseHelper.parseInSet(it, _builder);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("concept test");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("commonality Test {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("with AllElementTypes:Root");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("has foreignref referencing Referenced:Target {}");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      CommonalityReference _CommonalityReference = CommonalitiesLanguageCreators.commonalities.CommonalityReference();
      final Procedure1<CommonalityReference> _function_1 = (CommonalityReference it_1) -> {
        it_1.setName("foreignref");
        it_1.setReferenceType(referenced.getCommonality());
      };
      CommonalityReference _doubleArrow = ObjectExtensions.<CommonalityReference>operator_doubleArrow(_CommonalityReference, _function_1);
      MatcherAssert.<CommonalityReference>assertThat(this._commonalityParseHelper.parseAndValidateInSet(it, _builder_1).getCommonality().getReferences().get(0), ModelMatchers.<CommonalityReference>equalsDeeply(_doubleArrow, ModelMatchers.usingEqualsForReferencesTo(CommonalitiesLanguageCreators.commonalities.Commonality().eClass())));
    };
    this._commonalityParseHelper.inSameResourceSet(_function);
  }
}
