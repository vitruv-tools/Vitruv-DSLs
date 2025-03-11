package tools.vitruv.dsls.commonalities.tests.syntax;

import com.google.common.collect.Iterables;
import javax.inject.Inject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.language.LanguagePackage;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationPart;
import tools.vitruv.dsls.commonalities.language.ParticipationRelation;
import tools.vitruv.dsls.commonalities.tests.CommonalitiesLanguageInjectorProvider;
import tools.vitruv.dsls.commonalities.tests.util.CommonalitiesLanguageCreators;
import tools.vitruv.dsls.commonalities.tests.util.CommonalityParseHelper;
import tools.vitruv.testutils.matchers.ModelMatchers;

@ExtendWith(InjectionExtension.class)
@InjectWith(CommonalitiesLanguageInjectorProvider.class)
@DisplayName("parsing Participations")
@SuppressWarnings("all")
public class ParticipationParsingTest {
  @Inject
  @Extension
  private CommonalityParseHelper _commonalityParseHelper;

  @ParameterizedTest(name = "{0}")
  @DisplayName("single ParticipationClass")
  @ValueSource(strings = { "with theDomain:TheClass", "with (theDomain):TheClass", "with theDomain:(TheClass)", "with (theDomain):(TheClass)" })
  public void singleParticipation(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts.add(_ParticipationClass);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("single ParticipationClass with domain alias")
  @ValueSource(strings = { "with (theDomain as otherDomain):TheClass", "with (theDomain as otherDomain):(TheClass)" })
  public void singleParticipationWithDomainAlias(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      it.setDomainAlias("otherDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts.add(_ParticipationClass);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("single ParticipationClass with singleton")
  @ValueSource(strings = { "with theDomain:(single TheClass)", "with (theDomain):(single TheClass)" })
  public void singleParticipationWithSingleton(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_1 = (ParticipationClass it_1) -> {
        it_1.setSingleton(true);
      };
      ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass, _function_1);
      _parts.add(_doubleArrow);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("single ParticipationClass with class alias")
  @ValueSource(strings = { "with theDomain:(TheClass as OtherClass)", "with (theDomain):(TheClass as OtherClass)" })
  public void singleParticipationWithClassAlias(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_1 = (ParticipationClass it_1) -> {
        it_1.setClassAlias("OtherClass");
      };
      ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass, _function_1);
      _parts.add(_doubleArrow);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @Test
  @DisplayName("single Participation with domain alias, singleton, and class alias")
  public void singleParticipationAllFeatures() {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      it.setDomainAlias("otherDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_1 = (ParticipationClass it_1) -> {
        it_1.setSingleton(true);
        it_1.setClassAlias("OtherClass");
      };
      ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass, _function_1);
      _parts.add(_doubleArrow);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(
      this.parseParticipation(
        "with (theDomain as otherDomain):(single TheClass as OtherClass)"), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("multiple ParticipationClasses")
  @ValueSource(strings = { "with theDomain:(FirstClass, SecondClass, ThirdClass)", "with (theDomain):(FirstClass, SecondClass, ThirdClass)", "with theDomain:((FirstClass), SecondClass, ThirdClass)", "with theDomain:((FirstClass), (SecondClass), ThirdClass)", "with theDomain:(FirstClass, SecondClass, (ThirdClass))", "with (theDomain):((FirstClass), SecondClass, ThirdClass)", "with (theDomain):((FirstClass), SecondClass, (ThirdClass))", "with (theDomain):(FirstClass, SecondClass, (ThirdClass))" })
  public void tupleParticipation(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts.add(_ParticipationClass);
      EList<ParticipationPart> _parts_1 = it.getParts();
      ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts_1.add(_ParticipationClass_1);
      EList<ParticipationPart> _parts_2 = it.getParts();
      ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts_2.add(_ParticipationClass_2);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("multiple ParticipationClasses with domain alias")
  @ValueSource(strings = { "with (theDomain as otherDomain):(FirstClass, SecondClass, ThirdClass)", "with (theDomain as otherDomain):((FirstClass), SecondClass, ThirdClass)", "with (theDomain as otherDomain):((FirstClass), SecondClass, (ThirdClass))", "with (theDomain as otherDomain):(FirstClass, SecondClass, (ThirdClass))" })
  public void tupleParticipationWithDomainAlias(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      it.setDomainAlias("otherDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts.add(_ParticipationClass);
      EList<ParticipationPart> _parts_1 = it.getParts();
      ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts_1.add(_ParticipationClass_1);
      EList<ParticipationPart> _parts_2 = it.getParts();
      ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts_2.add(_ParticipationClass_2);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("multiple ParticipationClasses with class alias at the second position")
  @ValueSource(strings = { "with theDomain:(FirstClass, SecondClass as OtherSecondClass, ThirdClass)", "with (theDomain):(FirstClass, SecondClass as OtherSecondClass, ThirdClass)", "with theDomain:((FirstClass), SecondClass as OtherSecondClass, ThirdClass)", "with theDomain:((FirstClass), (SecondClass as OtherSecondClass), ThirdClass)", "with theDomain:(FirstClass, SecondClass as OtherSecondClass, (ThirdClass))", "with (theDomain):((FirstClass), SecondClass as OtherSecondClass, ThirdClass)", "with (theDomain):((FirstClass), SecondClass as OtherSecondClass, (ThirdClass))", "with (theDomain):(FirstClass, (SecondClass as OtherSecondClass), (ThirdClass))" })
  public void tupleParticipationWithClassAlias(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts.add(_ParticipationClass);
      EList<ParticipationPart> _parts_1 = it.getParts();
      ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_1 = (ParticipationClass it_1) -> {
        it_1.setClassAlias("OtherSecondClass");
      };
      ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_1, _function_1);
      _parts_1.add(_doubleArrow);
      EList<ParticipationPart> _parts_2 = it.getParts();
      ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts_2.add(_ParticipationClass_2);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("multiple ParticipationClasses with multiple class aliases")
  @ValueSource(strings = { "with theDomain:(FirstClass as OtherFirstClass, SecondClass, ThirdClass as OtherThirdClass)", "with (theDomain):(FirstClass as OtherFirstClass, SecondClass, ThirdClass as OtherThirdClass)", "with theDomain:((FirstClass as OtherFirstClass), SecondClass, ThirdClass as OtherThirdClass)", "with theDomain:((FirstClass as OtherFirstClass), (SecondClass), ThirdClass as OtherThirdClass)", "with theDomain:(FirstClass as OtherFirstClass, SecondClass, (ThirdClass as OtherThirdClass))", "with (theDomain):((FirstClass as OtherFirstClass), SecondClass, ThirdClass as OtherThirdClass)", "with (theDomain):((FirstClass as OtherFirstClass), SecondClass, (ThirdClass as OtherThirdClass))", "with (theDomain):(FirstClass as OtherFirstClass, SecondClass, (ThirdClass as OtherThirdClass))" })
  public void tupleParticipationWithClassAliases(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_1 = (ParticipationClass it_1) -> {
        it_1.setClassAlias("OtherFirstClass");
      };
      ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass, _function_1);
      _parts.add(_doubleArrow);
      EList<ParticipationPart> _parts_1 = it.getParts();
      ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts_1.add(_ParticipationClass_1);
      EList<ParticipationPart> _parts_2 = it.getParts();
      ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_2 = (ParticipationClass it_1) -> {
        it_1.setClassAlias("OtherThirdClass");
      };
      ParticipationClass _doubleArrow_1 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_2, _function_2);
      _parts_2.add(_doubleArrow_1);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("multiple ParticipationClasses with singleton")
  @ValueSource(strings = { "with theDomain:(FirstClass, single SecondClass, ThirdClass)", "with (theDomain):(FirstClass, single SecondClass, ThirdClass)", "with theDomain:((FirstClass), single SecondClass, ThirdClass)", "with theDomain:((FirstClass), (single SecondClass), ThirdClass)", "with theDomain:(FirstClass, single SecondClass, (ThirdClass))", "with (theDomain):((FirstClass), single SecondClass, ThirdClass)", "with (theDomain):((FirstClass), single SecondClass, (ThirdClass))", "with (theDomain):(FirstClass, single SecondClass, (ThirdClass))" })
  public void tupleParticipationWithSingleton(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts.add(_ParticipationClass);
      EList<ParticipationPart> _parts_1 = it.getParts();
      ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_1 = (ParticipationClass it_1) -> {
        it_1.setSingleton(true);
      };
      ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_1, _function_1);
      _parts_1.add(_doubleArrow);
      EList<ParticipationPart> _parts_2 = it.getParts();
      ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts_2.add(_ParticipationClass_2);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("multiple ParticipationClasses with multiple singletons")
  @ValueSource(strings = { "with theDomain:(single FirstClass, SecondClass, single ThirdClass)", "with (theDomain):(single FirstClass, SecondClass, single ThirdClass)", "with theDomain:((single FirstClass), SecondClass, single ThirdClass)", "with theDomain:((single FirstClass), (SecondClass), single ThirdClass)", "with theDomain:(single FirstClass, SecondClass, (single ThirdClass))", "with (theDomain):((single FirstClass), SecondClass, single ThirdClass)", "with (theDomain):((single FirstClass), SecondClass, (single ThirdClass))", "with (theDomain):(single FirstClass, SecondClass, (single ThirdClass))" })
  public void tupleParticipationWithSingletons(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_1 = (ParticipationClass it_1) -> {
        it_1.setSingleton(true);
      };
      ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass, _function_1);
      _parts.add(_doubleArrow);
      EList<ParticipationPart> _parts_1 = it.getParts();
      ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts_1.add(_ParticipationClass_1);
      EList<ParticipationPart> _parts_2 = it.getParts();
      ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_2 = (ParticipationClass it_1) -> {
        it_1.setSingleton(true);
      };
      ParticipationClass _doubleArrow_1 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_2, _function_2);
      _parts_2.add(_doubleArrow_1);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("multiple ParticipationClasses with domain alias, class alias and singleton")
  @ValueSource(strings = { "with (theDomain as otherDomain):(single FirstClass as OtherFirstClass, single SecondClass, ThirdClass as OtherThirdClass)", "with (theDomain as otherDomain):((single FirstClass as OtherFirstClass), single SecondClass, ThirdClass as OtherThirdClass)", "with (theDomain as otherDomain):((single FirstClass as OtherFirstClass), single SecondClass, (ThirdClass as OtherThirdClass))", "with (theDomain as otherDomain):(single FirstClass as OtherFirstClass, single SecondClass, (ThirdClass as OtherThirdClass))" })
  public void tupleParticipationWithAllFeatures(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      it.setDomainAlias("otherDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_1 = (ParticipationClass it_1) -> {
        it_1.setSingleton(true);
        it_1.setClassAlias("OtherFirstClass");
      };
      ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass, _function_1);
      _parts.add(_doubleArrow);
      EList<ParticipationPart> _parts_1 = it.getParts();
      ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_2 = (ParticipationClass it_1) -> {
        it_1.setSingleton(true);
      };
      ParticipationClass _doubleArrow_1 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_1, _function_2);
      _parts_1.add(_doubleArrow_1);
      EList<ParticipationPart> _parts_2 = it.getParts();
      ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      final Procedure1<ParticipationClass> _function_3 = (ParticipationClass it_1) -> {
        it_1.setClassAlias("OtherThirdClass");
      };
      ParticipationClass _doubleArrow_2 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_2, _function_3);
      _parts_2.add(_doubleArrow_2);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("single ParticipationRelation")
  @ValueSource(strings = { "with theDomain:(FirstClass op SecondClass)", "with (theDomain):(FirstClass op SecondClass)", "with theDomain:((FirstClass) op SecondClass)", "with (theDomain):(FirstClass op (SecondClass))", "with (theDomain):((FirstClass) op (SecondClass))" })
  public void singleParticipationRelation(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationRelation _ParticipationRelation = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_1 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _leftParts.add(_ParticipationClass);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _rightParts.add(_ParticipationClass_1);
      };
      ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation, _function_1);
      _parts.add(_doubleArrow);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("single ParticipationRelation with multiple operands left")
  @ValueSource(strings = { "with theDomain:((FirstClass, SecondClass, ThirdClass) op FourthClass)", "with theDomain:((FirstClass, SecondClass, ThirdClass) op (FourthClass))", "with theDomain:(((FirstClass), SecondClass, (ThirdClass)) op (FourthClass))", "with (theDomain):((FirstClass, SecondClass, ThirdClass) op FourthClass)", "with (theDomain):((FirstClass, SecondClass, ThirdClass) op (FourthClass))", "with (theDomain):(((FirstClass), SecondClass, (ThirdClass)) op (FourthClass))" })
  public void singleParticipationRelationsMultipleLefts(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationRelation _ParticipationRelation = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_1 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _leftParts.add(_ParticipationClass);
        EList<ParticipationPart> _leftParts_1 = it_1.getLeftParts();
        ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _leftParts_1.add(_ParticipationClass_1);
        EList<ParticipationPart> _leftParts_2 = it_1.getLeftParts();
        ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _leftParts_2.add(_ParticipationClass_2);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationClass _ParticipationClass_3 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _rightParts.add(_ParticipationClass_3);
      };
      ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation, _function_1);
      _parts.add(_doubleArrow);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("single ParticipationRelation with multiple operands right")
  @ValueSource(strings = { "with theDomain:(FirstClass op (SecondClass, ThirdClass, FourthClass))", "with theDomain:((FirstClass) op (SecondClass, ThirdClass, FourthClass))", "with theDomain:((FirstClass) op ((SecondClass), ThirdClass, (FourthClass)))", "with (theDomain):(FirstClass op (SecondClass, ThirdClass, FourthClass))", "with (theDomain):((FirstClass) op (SecondClass, ThirdClass, FourthClass))", "with (theDomain):((FirstClass) op ((SecondClass), ThirdClass, (FourthClass)))" })
  public void singleParticipationRelationsMultipleRights(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationRelation _ParticipationRelation = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_1 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _leftParts.add(_ParticipationClass);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _rightParts.add(_ParticipationClass_1);
        EList<ParticipationPart> _rightParts_1 = it_1.getRightParts();
        ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _rightParts_1.add(_ParticipationClass_2);
        EList<ParticipationPart> _rightParts_2 = it_1.getRightParts();
        ParticipationClass _ParticipationClass_3 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _rightParts_2.add(_ParticipationClass_3);
      };
      ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation, _function_1);
      _parts.add(_doubleArrow);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("single ParticipationRelation with multiple operands, singletons, domain aliases, and class aliases")
  @ValueSource(strings = { "with (theDomain as otherDomain):((single FirstClass as OtherClass, SecondClass) op (single ThirdClass, FourthClass as OtherFourthClass))", "with (theDomain as otherDomain):((single FirstClass as OtherClass, (SecondClass)) op (single ThirdClass, FourthClass as OtherFourthClass))", "with (theDomain as otherDomain):(((single FirstClass as OtherClass), (SecondClass)) op ((single ThirdClass), (FourthClass as OtherFourthClass)))" })
  public void singleParticipationRelationsAllFeatures(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      it.setDomainAlias("otherDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationRelation _ParticipationRelation = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_1 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        final Procedure1<ParticipationClass> _function_2 = (ParticipationClass it_2) -> {
          it_2.setSingleton(true);
          it_2.setClassAlias("OtherClass");
        };
        ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass, _function_2);
        _leftParts.add(_doubleArrow);
        EList<ParticipationPart> _leftParts_1 = it_1.getLeftParts();
        ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _leftParts_1.add(_ParticipationClass_1);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        final Procedure1<ParticipationClass> _function_3 = (ParticipationClass it_2) -> {
          it_2.setSingleton(true);
        };
        ParticipationClass _doubleArrow_1 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_2, _function_3);
        _rightParts.add(_doubleArrow_1);
        EList<ParticipationPart> _rightParts_1 = it_1.getRightParts();
        ParticipationClass _ParticipationClass_3 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        final Procedure1<ParticipationClass> _function_4 = (ParticipationClass it_2) -> {
          it_2.setClassAlias("OtherFourthClass");
        };
        ParticipationClass _doubleArrow_2 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_3, _function_4);
        _rightParts_1.add(_doubleArrow_2);
      };
      ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation, _function_1);
      _parts.add(_doubleArrow);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("multiple ParticipationRelations")
  @ValueSource(strings = { "with theDomain:(FirstClass op SecondClass, ThirdClass op FourthClass)", "with theDomain:((FirstClass) op SecondClass, ThirdClass op (FourthClass))", "with theDomain:((FirstClass) op (SecondClass), (ThirdClass) op (FourthClass))", "with (theDomain):(FirstClass op SecondClass, ThirdClass op FourthClass)", "with (theDomain):((FirstClass) op SecondClass, ThirdClass op (FourthClass))", "with (theDomain):((FirstClass) op (SecondClass), (ThirdClass) op (FourthClass))" })
  public void multipleParticipationRelations(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationRelation _ParticipationRelation = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_1 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _leftParts.add(_ParticipationClass);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _rightParts.add(_ParticipationClass_1);
      };
      ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation, _function_1);
      _parts.add(_doubleArrow);
      EList<ParticipationPart> _parts_1 = it.getParts();
      ParticipationRelation _ParticipationRelation_1 = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_2 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _leftParts.add(_ParticipationClass);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _rightParts.add(_ParticipationClass_1);
      };
      ParticipationRelation _doubleArrow_1 = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation_1, _function_2);
      _parts_1.add(_doubleArrow_1);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("multiple ParticipationRelations with multiple operands, singletons, domain aliases, and class aliases")
  @ValueSource(strings = { "with (theDomain as otherDomain):((Class1, single Class2 as OtherClass2) op Class3, (Class4 as OtherClass4, single Class5) op (Class6, single Class7))", "with (theDomain as otherDomain):(((Class1), single Class2 as OtherClass2) op (Class3), (Class4 as OtherClass4, single Class5) op ((Class6), single Class7))", "with (theDomain as otherDomain):(((Class1), (single Class2 as OtherClass2)) op (Class3), ((Class4 as OtherClass4), (single Class5)) op ((Class6), (single Class7)))" })
  public void multipleParticipationRelationsAllFeatures(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      it.setDomainAlias("otherDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationRelation _ParticipationRelation = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_1 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _leftParts.add(_ParticipationClass);
        EList<ParticipationPart> _leftParts_1 = it_1.getLeftParts();
        ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        final Procedure1<ParticipationClass> _function_2 = (ParticipationClass it_2) -> {
          it_2.setSingleton(true);
          it_2.setClassAlias("OtherClass2");
        };
        ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_1, _function_2);
        _leftParts_1.add(_doubleArrow);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _rightParts.add(_ParticipationClass_2);
      };
      ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation, _function_1);
      _parts.add(_doubleArrow);
      EList<ParticipationPart> _parts_1 = it.getParts();
      ParticipationRelation _ParticipationRelation_1 = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_2 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        final Procedure1<ParticipationClass> _function_3 = (ParticipationClass it_2) -> {
          it_2.setClassAlias("OtherClass4");
        };
        ParticipationClass _doubleArrow_1 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass, _function_3);
        _leftParts.add(_doubleArrow_1);
        EList<ParticipationPart> _leftParts_1 = it_1.getLeftParts();
        ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        final Procedure1<ParticipationClass> _function_4 = (ParticipationClass it_2) -> {
          it_2.setSingleton(true);
        };
        ParticipationClass _doubleArrow_2 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_1, _function_4);
        _leftParts_1.add(_doubleArrow_2);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _rightParts.add(_ParticipationClass_2);
        EList<ParticipationPart> _rightParts_1 = it_1.getRightParts();
        ParticipationClass _ParticipationClass_3 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        final Procedure1<ParticipationClass> _function_5 = (ParticipationClass it_2) -> {
          it_2.setSingleton(true);
        };
        ParticipationClass _doubleArrow_3 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_3, _function_5);
        _rightParts_1.add(_doubleArrow_3);
      };
      ParticipationRelation _doubleArrow_1 = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation_1, _function_2);
      _parts_1.add(_doubleArrow_1);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("right nested ParticipationRelations")
  @ValueSource(strings = { "with theDomain:(FirstClass op (SecondClass op (ThirdClass op FourthClass)))", "with theDomain:(FirstClass op ((SecondClass) op (ThirdClass op (FourthClass))))", "with theDomain:((FirstClass) op ((SecondClass) op ((ThirdClass) op (FourthClass))))", "with (theDomain):(FirstClass op (SecondClass op (ThirdClass op FourthClass)))", "with (theDomain):(FirstClass op ((SecondClass) op (ThirdClass op (FourthClass))))", "with (theDomain):((FirstClass) op ((SecondClass) op ((ThirdClass) op (FourthClass))))" })
  public void participationRelationsRightNested(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationRelation _ParticipationRelation = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_1 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _leftParts.add(_ParticipationClass);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationRelation _ParticipationRelation_1 = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
        final Procedure1<ParticipationRelation> _function_2 = (ParticipationRelation it_2) -> {
          EList<ParticipationPart> _leftParts_1 = it_2.getLeftParts();
          ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
          _leftParts_1.add(_ParticipationClass_1);
          EList<ParticipationPart> _rightParts_1 = it_2.getRightParts();
          ParticipationRelation _ParticipationRelation_2 = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
          final Procedure1<ParticipationRelation> _function_3 = (ParticipationRelation it_3) -> {
            EList<ParticipationPart> _leftParts_2 = it_3.getLeftParts();
            ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
            _leftParts_2.add(_ParticipationClass_2);
            EList<ParticipationPart> _rightParts_2 = it_3.getRightParts();
            ParticipationClass _ParticipationClass_3 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
            _rightParts_2.add(_ParticipationClass_3);
          };
          ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation_2, _function_3);
          _rightParts_1.add(_doubleArrow);
        };
        ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation_1, _function_2);
        _rightParts.add(_doubleArrow);
      };
      ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation, _function_1);
      _parts.add(_doubleArrow);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @ParameterizedTest(name = "{0}")
  @DisplayName("left nested ParticipationRelations")
  @ValueSource(strings = { "with theDomain:(((FirstClass op SecondClass) op ThirdClass) op FourthClass)", "with theDomain:(((FirstClass op (SecondClass)) op ThirdClass) op (FourthClass))", "with theDomain:((((FirstClass) op (SecondClass)) op (ThirdClass)) op (FourthClass))", "with (theDomain):(((FirstClass op SecondClass) op ThirdClass) op FourthClass)", "with (theDomain):(((FirstClass op (SecondClass)) op ThirdClass) op (FourthClass))", "with (theDomain):((((FirstClass) op (SecondClass)) op (ThirdClass)) op (FourthClass))" })
  public void participationRelationsleftNested(final String participationDeclaration) {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("theDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationRelation _ParticipationRelation = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_1 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationRelation _ParticipationRelation_1 = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
        final Procedure1<ParticipationRelation> _function_2 = (ParticipationRelation it_2) -> {
          EList<ParticipationPart> _leftParts_1 = it_2.getLeftParts();
          ParticipationRelation _ParticipationRelation_2 = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
          final Procedure1<ParticipationRelation> _function_3 = (ParticipationRelation it_3) -> {
            EList<ParticipationPart> _leftParts_2 = it_3.getLeftParts();
            ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
            _leftParts_2.add(_ParticipationClass);
            EList<ParticipationPart> _rightParts = it_3.getRightParts();
            ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
            _rightParts.add(_ParticipationClass_1);
          };
          ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation_2, _function_3);
          _leftParts_1.add(_doubleArrow);
          EList<ParticipationPart> _rightParts = it_2.getRightParts();
          ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
          _rightParts.add(_ParticipationClass);
        };
        ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation_1, _function_2);
        _leftParts.add(_doubleArrow);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _rightParts.add(_ParticipationClass);
      };
      ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation, _function_1);
      _parts.add(_doubleArrow);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    MatcherAssert.<CommonalityFile>assertThat(this.parseParticipation(participationDeclaration), this.equalsParticipation(_doubleArrow));
  }

  @Test
  @DisplayName("multiple participations, with relations, singletons, domain alias, and class aliases")
  public void allFeatures() {
    Participation _Participation = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function = (Participation it) -> {
      it.setDomainName("firstDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationRelation _ParticipationRelation = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_1 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationRelation _ParticipationRelation_1 = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
        final Procedure1<ParticipationRelation> _function_2 = (ParticipationRelation it_2) -> {
          EList<ParticipationPart> _leftParts_1 = it_2.getLeftParts();
          ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
          _leftParts_1.add(_ParticipationClass);
          EList<ParticipationPart> _rightParts = it_2.getRightParts();
          ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
          final Procedure1<ParticipationClass> _function_3 = (ParticipationClass it_3) -> {
            it_3.setSingleton(true);
          };
          ParticipationClass _doubleArrow = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_1, _function_3);
          _rightParts.add(_doubleArrow);
        };
        ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation_1, _function_2);
        _leftParts.add(_doubleArrow);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationRelation _ParticipationRelation_2 = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
        final Procedure1<ParticipationRelation> _function_3 = (ParticipationRelation it_2) -> {
          EList<ParticipationPart> _leftParts_1 = it_2.getLeftParts();
          ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
          _leftParts_1.add(_ParticipationClass);
          EList<ParticipationPart> _rightParts_1 = it_2.getRightParts();
          ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
          _rightParts_1.add(_ParticipationClass_1);
        };
        ParticipationRelation _doubleArrow_1 = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation_2, _function_3);
        _rightParts.add(_doubleArrow_1);
      };
      ParticipationRelation _doubleArrow = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation, _function_1);
      _parts.add(_doubleArrow);
    };
    Participation _doubleArrow = ObjectExtensions.<Participation>operator_doubleArrow(_Participation, _function);
    Participation _Participation_1 = CommonalitiesLanguageCreators.commonalities.Participation();
    final Procedure1<Participation> _function_1 = (Participation it) -> {
      it.setDomainName("theDomain");
      it.setDomainAlias("secondDomain");
      EList<ParticipationPart> _parts = it.getParts();
      ParticipationClass _ParticipationClass = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
      _parts.add(_ParticipationClass);
      EList<ParticipationPart> _parts_1 = it.getParts();
      ParticipationRelation _ParticipationRelation = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_2 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        final Procedure1<ParticipationClass> _function_3 = (ParticipationClass it_2) -> {
          it_2.setSingleton(true);
        };
        ParticipationClass _doubleArrow_1 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_1, _function_3);
        _leftParts.add(_doubleArrow_1);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        final Procedure1<ParticipationClass> _function_4 = (ParticipationClass it_2) -> {
          it_2.setClassAlias("ThirdClass");
        };
        ParticipationClass _doubleArrow_2 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_2, _function_4);
        _rightParts.add(_doubleArrow_2);
        EList<ParticipationPart> _rightParts_1 = it_1.getRightParts();
        ParticipationRelation _ParticipationRelation_1 = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
        final Procedure1<ParticipationRelation> _function_5 = (ParticipationRelation it_2) -> {
          EList<ParticipationPart> _leftParts_1 = it_2.getLeftParts();
          ParticipationClass _ParticipationClass_3 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
          _leftParts_1.add(_ParticipationClass_3);
          EList<ParticipationPart> _rightParts_2 = it_2.getRightParts();
          ParticipationClass _ParticipationClass_4 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
          final Procedure1<ParticipationClass> _function_6 = (ParticipationClass it_3) -> {
            it_3.setSingleton(true);
            it_3.setClassAlias("FifthClass");
          };
          ParticipationClass _doubleArrow_3 = ObjectExtensions.<ParticipationClass>operator_doubleArrow(_ParticipationClass_4, _function_6);
          _rightParts_2.add(_doubleArrow_3);
        };
        ParticipationRelation _doubleArrow_3 = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation_1, _function_5);
        _rightParts_1.add(_doubleArrow_3);
      };
      ParticipationRelation _doubleArrow_1 = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation, _function_2);
      _parts_1.add(_doubleArrow_1);
      EList<ParticipationPart> _parts_2 = it.getParts();
      ParticipationRelation _ParticipationRelation_1 = CommonalitiesLanguageCreators.commonalities.ParticipationRelation();
      final Procedure1<ParticipationRelation> _function_3 = (ParticipationRelation it_1) -> {
        EList<ParticipationPart> _leftParts = it_1.getLeftParts();
        ParticipationClass _ParticipationClass_1 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _leftParts.add(_ParticipationClass_1);
        EList<ParticipationPart> _rightParts = it_1.getRightParts();
        ParticipationClass _ParticipationClass_2 = CommonalitiesLanguageCreators.commonalities.ParticipationClass();
        _rightParts.add(_ParticipationClass_2);
      };
      ParticipationRelation _doubleArrow_2 = ObjectExtensions.<ParticipationRelation>operator_doubleArrow(_ParticipationRelation_1, _function_3);
      _parts_2.add(_doubleArrow_2);
    };
    Participation _doubleArrow_1 = ObjectExtensions.<Participation>operator_doubleArrow(_Participation_1, _function_1);
    MatcherAssert.<CommonalityFile>assertThat(
      this.parseParticipation(
        "with firstDomain:((FirstClass op1 (single SecondClass)) op2 (ThirdClass op3 FourthClass))", 
        "with (theDomain as secondDomain):(FirstClass, (single SecondClass) op1 (SomeClass as ThirdClass, FourthClass op2 (single SomeClass as FifthClass)), SixthClass op3 SeventhClass)"), this.equalsParticipation(_doubleArrow, _doubleArrow_1));
  }

  private CommonalityFile parseParticipation(final String... participations) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("concept test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("commonality Example {");
    _builder.newLine();
    _builder.append("\t");
    {
      boolean _hasElements = false;
      for(final String participation : participations) {
        if (!_hasElements) {
          _hasElements = true;
        } else {
          String _lineSeparator = System.lineSeparator();
          _builder.appendImmediate(_lineSeparator, "\t");
        }
        _builder.append(participation, "\t");
      }
    }
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    _builder.newLine();
    return this._commonalityParseHelper.parse(_builder);
  }

  private Matcher<? super CommonalityFile> equalsParticipation(final Participation... expectedParticipations) {
    CommonalityFile _CommonalityFile = CommonalitiesLanguageCreators.commonalities.CommonalityFile();
    final Procedure1<CommonalityFile> _function = (CommonalityFile it) -> {
      Concept _Concept = CommonalitiesLanguageCreators.commonalities.Concept();
      final Procedure1<Concept> _function_1 = (Concept it_1) -> {
        it_1.setName("test");
      };
      Concept _doubleArrow = ObjectExtensions.<Concept>operator_doubleArrow(_Concept, _function_1);
      it.setConcept(_doubleArrow);
      Commonality _Commonality = CommonalitiesLanguageCreators.commonalities.Commonality();
      final Procedure1<Commonality> _function_2 = (Commonality it_1) -> {
        it_1.setName("Example");
        EList<Participation> _participations = it_1.getParticipations();
        Iterables.<Participation>addAll(_participations, ((Iterable<? extends Participation>)Conversions.doWrapArray(expectedParticipations)));
      };
      Commonality _doubleArrow_1 = ObjectExtensions.<Commonality>operator_doubleArrow(_Commonality, _function_2);
      it.setCommonality(_doubleArrow_1);
    };
    CommonalityFile _doubleArrow = ObjectExtensions.<CommonalityFile>operator_doubleArrow(_CommonalityFile, _function);
    return ModelMatchers.<CommonalityFile>equalsDeeply(_doubleArrow, ModelMatchers.ignoringFeatures(LanguagePackage.Literals.PARTICIPATION_CLASS__SUPER_METACLASS, LanguagePackage.Literals.PARTICIPATION_RELATION__OPERATOR));
  }
}
