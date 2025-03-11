package tools.vitruv.dsls.commonalities.tests.syntax;

import com.google.inject.Inject;
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
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.tests.CommonalitiesLanguageInjectorProvider;
import tools.vitruv.dsls.commonalities.tests.util.CommonalitiesLanguageCreators;
import tools.vitruv.dsls.commonalities.tests.util.CommonalityParseHelper;
import tools.vitruv.testutils.matchers.ModelMatchers;

@ExtendWith(InjectionExtension.class)
@InjectWith(CommonalitiesLanguageInjectorProvider.class)
@DisplayName("parsing whole Commonalities")
@SuppressWarnings("all")
public class CommonalityParsingTest {
  @Inject
  @Extension
  private CommonalityParseHelper _commonalityParseHelper;

  @Test
  @DisplayName("parses a minimal commonality")
  public void minimalCommonality() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("concept test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("commonality Example {}");
    _builder.newLine();
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
      };
      Commonality _doubleArrow_1 = ObjectExtensions.<Commonality>operator_doubleArrow(_Commonality, _function_2);
      it.setCommonality(_doubleArrow_1);
    };
    CommonalityFile _doubleArrow = ObjectExtensions.<CommonalityFile>operator_doubleArrow(_CommonalityFile, _function);
    MatcherAssert.<CommonalityFile>assertThat(this._commonalityParseHelper.parse(_builder), ModelMatchers.<CommonalityFile>equalsDeeply(_doubleArrow));
  }
}
