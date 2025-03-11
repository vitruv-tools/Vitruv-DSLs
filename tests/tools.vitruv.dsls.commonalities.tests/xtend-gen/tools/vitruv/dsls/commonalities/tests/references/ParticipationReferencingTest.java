package tools.vitruv.dsls.commonalities.tests.references;

import allElementTypes.AllElementTypesPackage;
import javax.inject.Inject;
import org.eclipse.emf.common.util.EList;
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
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationPart;
import tools.vitruv.dsls.commonalities.tests.CommonalitiesLanguageInjectorProvider;
import tools.vitruv.dsls.commonalities.tests.util.CommonalitiesLanguageCreators;
import tools.vitruv.dsls.commonalities.tests.util.CommonalitiesLanguageElementsPrinter;
import tools.vitruv.dsls.commonalities.tests.util.CommonalityParseHelper;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.testutils.printing.ModelPrinterChange;
import tools.vitruv.testutils.printing.UseModelPrinter;

@ExtendWith({ InjectionExtension.class, ModelPrinterChange.class })
@InjectWith(CommonalitiesLanguageInjectorProvider.class)
@UseModelPrinter(CommonalitiesLanguageElementsPrinter.class)
@DisplayName("referencing domains in Participations")
@SuppressWarnings("all")
public class ParticipationReferencingTest {
  @Inject
  @Extension
  private CommonalityParseHelper _commonalityParseHelper;

  @Test
  @DisplayName("resolves a reference to a VitruvDomain")
  public void domainReference() {
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
    _builder.append("}");
    _builder.newLine();
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("AllElementTypes");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_1 = (ParticipationClass it_1) -> {
        it_1.setSuperMetaclass(CommonalitiesLanguageCreators.commonalities.languageElements.EClassMetaclass().forEClass(AllElementTypesCreators.aet.Root().eClass()).fromDomain(CommonalitiesLanguageCreators.commonalities.languageElements.Metamodel().forEPackage(AllElementTypesPackage.eINSTANCE)));
      };
      ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass, _function_1);
      _parts.add(_doubleArrow);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<Participation>assertThat(this._commonalityParseHelper.parseAndValidate(_builder).getCommonality().getParticipations().get(0), ModelMatchers.<Participation>equalsDeeply(_doubleArrow, ModelMatchers.usingEqualsForReferencesTo(CommonalitiesLanguageCreators.commonalities.languageElements.EClassMetaclass().eClass())));
  }

  @Test
  @DisplayName("resolves a reference to another Commonality")
  public void commoanlityReference() {
    final Procedure1<ResourceSet> _function = (ResourceSet it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("concept Referenced");
      _builder.newLine();
      _builder.newLine();
      _builder.append("commonality Target {}");
      _builder.newLine();
      final CommonalityFile referenced = this._commonalityParseHelper.parseInSet(it, _builder);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("concept test");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("commonality Test {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("with Referenced:Target");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
      final Procedure1<Participation> _function_1 = (Participation it_1) -> {
        it_1.setDomainName("Referenced");
        EList<ParticipationPart> _parts = it_1.getParts();
        ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        final Procedure1<ParticipationClass> _function_2 = (ParticipationClass it_2) -> {
          it_2.setSuperMetaclass(referenced.getCommonality());
        };
        ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass, _function_2);
        _parts.add(_doubleArrow);
      };
      Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function_1);
      MatcherAssert.<Participation>assertThat(this._commonalityParseHelper.parseAndValidateInSet(it, _builder_1).getCommonality().getParticipations().get(0), ModelMatchers.<Participation>equalsDeeply(_doubleArrow));
    };
    this._commonalityParseHelper.inSameResourceSet(_function);
  }
}
