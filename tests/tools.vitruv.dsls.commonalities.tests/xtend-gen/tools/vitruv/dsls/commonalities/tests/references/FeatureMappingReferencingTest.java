package tools.vitruv.dsls.commonalities.tests.references;

import allElementTypes.AllElementTypesPackage;
import javax.inject.Inject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.vitruv.dsls.commonalities.language.AttributeMappingOperand;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeMapping;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.dsls.commonalities.language.CommonalityReferenceMapping;
import tools.vitruv.dsls.commonalities.language.LanguagePackage;
import tools.vitruv.dsls.commonalities.language.OperatorAttributeMapping;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ReferenceMappingOperand;
import tools.vitruv.dsls.commonalities.language.ReferencedParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.SimpleAttributeMapping;
import tools.vitruv.dsls.commonalities.language.SimpleReferenceMapping;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.tests.CommonalitiesLanguageInjectorProvider;
import tools.vitruv.dsls.commonalities.tests.util.CommonalitiesLanguageCreators;
import tools.vitruv.dsls.commonalities.tests.util.CommonalitiesLanguageElementsPrinter;
import tools.vitruv.dsls.commonalities.tests.util.CommonalityParseHelper;
import tools.vitruv.testutils.matchers.EqualityStrategy;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.metamodels.AllElementTypesCreators;
import tools.vitruv.testutils.printing.ModelPrinterChange;
import tools.vitruv.testutils.printing.UseModelPrinter;

@ExtendWith({ InjectionExtension.class, ModelPrinterChange.class })
@InjectWith(CommonalitiesLanguageInjectorProvider.class)
@UseModelPrinter(CommonalitiesLanguageElementsPrinter.class)
@DisplayName("referencing Participations in features")
@SuppressWarnings("all")
public class FeatureMappingReferencingTest {
  @Inject
  @Extension
  private CommonalityParseHelper _commonalityParseHelper;

  @ParameterizedTest(name = "participation = \"{0}\", reference = \"{1}\"")
  @DisplayName("reference a Participation attribute in a SimpleAttributeMapping")
  @CsvSource({ "AllElementTypes:Root,AllElementTypes:Root.id", "(AllElementTypes as AET):(Root as Start),AET:Start.id" })
  public void referenceParticipationInSimpleAttributeMapping(final String participation, final String reference) {
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
    _builder.append("with ");
    _builder.append(participation, "\t");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("has name {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= ");
    _builder.append(reference, "\t\t");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final Commonality commonality = this._commonalityParseHelper.parseAndValidate(_builder).getCommonality();
    SimpleAttributeMapping _SimpleAttributeMapping = CommonalitiesLanguageCreators.commonalities.SimpleAttributeMapping();
    final Procedure1<SimpleAttributeMapping> _function = (SimpleAttributeMapping it) -> {
      it.setReadAndWrite(true);
      ParticipationAttribute _ParticipationAttribute = CommonalitiesLanguageCreators.commonalities.ParticipationAttribute();
      final Procedure1<ParticipationAttribute> _function_1 = (ParticipationAttribute it_1) -> {
        it_1.setParticipationClass(((ParticipationClass[])Conversions.unwrapArray(CommonalitiesLanguageModelExtensions.getAllClasses(commonality.getParticipations().get(0)), ParticipationClass.class))[0]);
        it_1.setAttribute(CommonalitiesLanguageCreators.commonalities.languageElements.EFeatureAttribute().fromMetaclass(
          CommonalitiesLanguageCreators.commonalities.languageElements.EClassMetaclass().fromDomain(CommonalitiesLanguageCreators.commonalities.languageElements.Metamodel().forEPackage(AllElementTypesPackage.eINSTANCE)).forEClass(AllElementTypesCreators.aet.Root().eClass())).forEFeature(AllElementTypesPackage.Literals.IDENTIFIED__ID));
      };
      ParticipationAttribute _doubleArrow = ObjectExtensions.<ParticipationAttribute>operator_doubleArrow(_ParticipationAttribute, _function_1);
      it.setAttribute(_doubleArrow);
    };
    SimpleAttributeMapping _doubleArrow = ObjectExtensions.<SimpleAttributeMapping>operator_doubleArrow(_SimpleAttributeMapping, _function);
    MatcherAssert.<CommonalityAttributeMapping>assertThat(commonality.getAttributes().get(0).getMappings().get(0), ModelMatchers.<CommonalityAttributeMapping>equalsDeeply(_doubleArrow, FeatureMappingReferencingTest.usingEqualsForEmfReferences()));
  }

  @ParameterizedTest(name = "participation = \"{0}\", reference = \"{1}\"")
  @DisplayName("reference a Participation attribute in a SimpleReferenceMapping")
  @CsvSource({ "AllElementTypes:Root,AllElementTypes:Root.recursiveRoot", "(AllElementTypes as AET):(Root as Start),AET:Start.recursiveRoot" })
  public void referenceParticipationInSimpleReferenceMapping(final String participation, final String reference) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("\t");
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
    _builder.newLine();
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("concept test");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("commonality Test {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("with ");
    _builder.append(participation, "\t\t");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("has selfref referencing test:Test {");
    _builder.newLine();
    _builder.append("\t\t\t ");
    _builder.append("= ");
    _builder.append(reference, "\t\t\t ");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    final Commonality commonality = this._commonalityParseHelper.parseAndValidate(_builder).getCommonality();
    SimpleReferenceMapping _SimpleReferenceMapping = CommonalitiesLanguageCreators.commonalities.SimpleReferenceMapping();
    final Procedure1<SimpleReferenceMapping> _function = (SimpleReferenceMapping it) -> {
      it.setReadAndWrite(true);
      ParticipationAttribute _ParticipationAttribute = CommonalitiesLanguageCreators.commonalities.ParticipationAttribute();
      final Procedure1<ParticipationAttribute> _function_1 = (ParticipationAttribute it_1) -> {
        it_1.setParticipationClass(((ParticipationClass[])Conversions.unwrapArray(CommonalitiesLanguageModelExtensions.getAllClasses(commonality.getParticipations().get(0)), ParticipationClass.class))[0]);
        it_1.setAttribute(CommonalitiesLanguageCreators.commonalities.languageElements.EFeatureAttribute().fromMetaclass(
          CommonalitiesLanguageCreators.commonalities.languageElements.EClassMetaclass().fromDomain(CommonalitiesLanguageCreators.commonalities.languageElements.Metamodel().forEPackage(AllElementTypesPackage.eINSTANCE)).forEClass(AllElementTypesCreators.aet.Root().eClass())).forEFeature(AllElementTypesPackage.Literals.ROOT__RECURSIVE_ROOT));
      };
      ParticipationAttribute _doubleArrow = ObjectExtensions.<ParticipationAttribute>operator_doubleArrow(_ParticipationAttribute, _function_1);
      it.setReference(_doubleArrow);
    };
    SimpleReferenceMapping _doubleArrow = ObjectExtensions.<SimpleReferenceMapping>operator_doubleArrow(_SimpleReferenceMapping, _function);
    MatcherAssert.<CommonalityReferenceMapping>assertThat(commonality.getReferences().get(0).getMappings().get(0), ModelMatchers.<CommonalityReferenceMapping>equalsDeeply(_doubleArrow, FeatureMappingReferencingTest.usingEqualsForEmfReferences()));
  }

  @ParameterizedTest(name = "participation = \"{0}\", reference = \"{1}\"")
  @DisplayName("reference a Participation attribute in an OperatorAttributeMapping")
  @CsvSource({ "AllElementTypes:Root,AllElementTypes:Root.singleValuedEAttribute", "(AllElementTypes as AET):(Root as Start),AET:Start.singleValuedEAttribute" })
  public void referenceParticipationInOperatorAttributeMapping(final String participation, final String reference) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import tools.vitruv.dsls.commonalities.tests.operators.digits");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.newLine();
    _builder.append("concept test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("commonality Test {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with ");
    _builder.append(participation, "\t");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("has digits {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("= digits(");
    _builder.append(reference, "\t\t");
    _builder.append(")");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final Commonality commonality = this._commonalityParseHelper.parseAndValidate(_builder).getCommonality();
    OperatorAttributeMapping _OperatorAttributeMapping = CommonalitiesLanguageCreators.commonalities.OperatorAttributeMapping();
    final Procedure1<OperatorAttributeMapping> _function = (OperatorAttributeMapping it) -> {
      it.setReadAndWrite(true);
      EList<AttributeMappingOperand> _operands = it.getOperands();
      ParticipationAttributeOperand _ParticipationAttributeOperand = CommonalitiesLanguageCreators.commonalities.ParticipationAttributeOperand();
      final Procedure1<ParticipationAttributeOperand> _function_1 = (ParticipationAttributeOperand it_1) -> {
        ParticipationAttribute _ParticipationAttribute = CommonalitiesLanguageCreators.commonalities.ParticipationAttribute();
        final Procedure1<ParticipationAttribute> _function_2 = (ParticipationAttribute it_2) -> {
          it_2.setParticipationClass(((ParticipationClass[])Conversions.unwrapArray(CommonalitiesLanguageModelExtensions.getAllClasses(commonality.getParticipations().get(0)), ParticipationClass.class))[0]);
          it_2.setAttribute(CommonalitiesLanguageCreators.commonalities.languageElements.EFeatureAttribute().fromMetaclass(
            CommonalitiesLanguageCreators.commonalities.languageElements.EClassMetaclass().fromDomain(CommonalitiesLanguageCreators.commonalities.languageElements.Metamodel().forEPackage(AllElementTypesPackage.eINSTANCE)).forEClass(AllElementTypesCreators.aet.Root().eClass())).forEFeature(AllElementTypesPackage.Literals.ROOT__SINGLE_VALUED_EATTRIBUTE));
        };
        ParticipationAttribute _doubleArrow = ObjectExtensions.<ParticipationAttribute>operator_doubleArrow(_ParticipationAttribute, _function_2);
        it_1.setParticipationAttribute(_doubleArrow);
      };
      ParticipationAttributeOperand _doubleArrow = ObjectExtensions.<ParticipationAttributeOperand>operator_doubleArrow(_ParticipationAttributeOperand, _function_1);
      _operands.add(_doubleArrow);
    };
    OperatorAttributeMapping _doubleArrow = ObjectExtensions.<OperatorAttributeMapping>operator_doubleArrow(_OperatorAttributeMapping, _function);
    MatcherAssert.<CommonalityAttributeMapping>assertThat(commonality.getAttributes().get(0).getMappings().get(0), ModelMatchers.<CommonalityAttributeMapping>equalsDeeply(_doubleArrow, FeatureMappingReferencingTest.usingEqualsForEmfReferences(), ModelMatchers.ignoringFeatures(LanguagePackage.Literals.OPERATOR_ATTRIBUTE_MAPPING__OPERATOR)));
  }

  @ParameterizedTest(name = "participation = \"{0}\", class reference = \"{1}\", attribute reference = \"{2}\"")
  @DisplayName("reference a Participation class and attribute in an OperatorReferenceMapping")
  @CsvSource(delimiter = ';', value = { "AllElementTypes:(Root, NonRoot);AllElementTypes:Root;NonRoot.id", "(AllElementTypes as AET):(Root as Start, NonRoot as End);AET:Start;End.id" })
  public void referenceParticipationInOperatorReferenceMapping(final String participation, final String classReference, final String attributeReference) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import tools.vitruv.dsls.commonalities.tests.operators.mock");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.newLine();
    _builder.append("concept test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("commonality Test {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("with ");
    _builder.append(participation, "\t");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("has selfref referencing test:Test {");
    _builder.newLine();
    _builder.append("\t\t ");
    _builder.append("= ");
    _builder.append(classReference, "\t\t ");
    _builder.append(".mock(");
    _builder.append(attributeReference, "\t\t ");
    _builder.append(")");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final Commonality commonality = this._commonalityParseHelper.parseAndValidate(_builder).getCommonality();
    OperatorReferenceMapping _OperatorReferenceMapping = CommonalitiesLanguageCreators.commonalities.OperatorReferenceMapping();
    final Procedure1<OperatorReferenceMapping> _function = (OperatorReferenceMapping it) -> {
      it.setReadAndWrite(true);
      it.setParticipationClass(((ParticipationClass[])Conversions.unwrapArray(CommonalitiesLanguageModelExtensions.getAllClasses(commonality.getParticipations().get(0)), ParticipationClass.class))[0]);
      EList<ReferenceMappingOperand> _operands = it.getOperands();
      ParticipationAttributeOperand _ParticipationAttributeOperand = CommonalitiesLanguageCreators.commonalities.ParticipationAttributeOperand();
      final Procedure1<ParticipationAttributeOperand> _function_1 = (ParticipationAttributeOperand it_1) -> {
        ParticipationAttribute _ParticipationAttribute = CommonalitiesLanguageCreators.commonalities.ParticipationAttribute();
        final Procedure1<ParticipationAttribute> _function_2 = (ParticipationAttribute it_2) -> {
          it_2.setParticipationClass(((ParticipationClass[])Conversions.unwrapArray(CommonalitiesLanguageModelExtensions.getAllClasses(commonality.getParticipations().get(0)), ParticipationClass.class))[1]);
          it_2.setAttribute(CommonalitiesLanguageCreators.commonalities.languageElements.EFeatureAttribute().fromMetaclass(
            CommonalitiesLanguageCreators.commonalities.languageElements.EClassMetaclass().fromDomain(CommonalitiesLanguageCreators.commonalities.languageElements.Metamodel().forEPackage(AllElementTypesPackage.eINSTANCE)).forEClass(AllElementTypesCreators.aet.NonRoot().eClass())).forEFeature(AllElementTypesPackage.Literals.IDENTIFIED__ID));
        };
        ParticipationAttribute _doubleArrow = ObjectExtensions.<ParticipationAttribute>operator_doubleArrow(_ParticipationAttribute, _function_2);
        it_1.setParticipationAttribute(_doubleArrow);
      };
      ParticipationAttributeOperand _doubleArrow = ObjectExtensions.<ParticipationAttributeOperand>operator_doubleArrow(_ParticipationAttributeOperand, _function_1);
      _operands.add(_doubleArrow);
    };
    OperatorReferenceMapping _doubleArrow = ObjectExtensions.<OperatorReferenceMapping>operator_doubleArrow(_OperatorReferenceMapping, _function);
    MatcherAssert.<CommonalityReferenceMapping>assertThat(commonality.getReferences().get(0).getMappings().get(0), ModelMatchers.<CommonalityReferenceMapping>equalsDeeply(_doubleArrow, FeatureMappingReferencingTest.usingEqualsForEmfReferences(), ModelMatchers.ignoringFeatures(LanguagePackage.Literals.OPERATOR_REFERENCE_MAPPING__OPERATOR)));
  }

  @ParameterizedTest(name = "participation = \"{0}\", referenced participation = \"{1}\", class reference = \"{2}\", attribute reference = \"{3}\"")
  @DisplayName("reference a referenced Participation class in an OperatorReferenceMapping")
  @CsvSource(delimiter = ';', value = { "AllElementTypes:Root;AllElementTypes:NonRoot;AllElementTypes:Root;NonRoot.id", "(AllElementTypes as AET):(Root as Start);(AllElementTypes as Elements):(NonRoot as End);AET:Start;End.id" })
  public void referenceReferencedParticipationInOperatorReferenceMapping(final String participation, final String referencedParticipation, final String classReference, final String attributeReference) {
    final Procedure1<ResourceSet> _function = (ResourceSet it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
      _builder.newLine();
      _builder.newLine();
      _builder.append("concept Referenced ");
      _builder.newLine();
      _builder.newLine();
      _builder.append("commonality Target {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("with ");
      _builder.append(referencedParticipation, "\t");
      _builder.newLineIfNotEmpty();
      _builder.append("}");
      _builder.newLine();
      final CommonalityFile referenced = this._commonalityParseHelper.parseInSet(it, _builder);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("import tools.vitruv.dsls.commonalities.tests.operators.mock");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("import \"http://tools.vitruv.testutils.metamodels.allElementTypes\" as AllElementTypes");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("concept test");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("commonality Test {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("with ");
      _builder_1.append(participation, "\t");
      _builder_1.newLineIfNotEmpty();
      _builder_1.append("\t");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("has foreignref referencing Referenced:Target {");
      _builder_1.newLine();
      _builder_1.append("\t\t ");
      _builder_1.append("= ");
      _builder_1.append(classReference, "\t\t ");
      _builder_1.append(".mock(ref ");
      _builder_1.append(attributeReference, "\t\t ");
      _builder_1.append(")");
      _builder_1.newLineIfNotEmpty();
      _builder_1.append("\t");
      _builder_1.append("}");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      final Commonality commonality = this._commonalityParseHelper.parseAndValidateInSet(it, _builder_1).getCommonality();
      OperatorReferenceMapping _OperatorReferenceMapping = CommonalitiesLanguageCreators.commonalities.OperatorReferenceMapping();
      final Procedure1<OperatorReferenceMapping> _function_1 = (OperatorReferenceMapping it_1) -> {
        it_1.setReadAndWrite(true);
        it_1.setParticipationClass(((ParticipationClass[])Conversions.unwrapArray(CommonalitiesLanguageModelExtensions.getAllClasses(commonality.getParticipations().get(0)), ParticipationClass.class))[0]);
        EList<ReferenceMappingOperand> _operands = it_1.getOperands();
        ReferencedParticipationAttributeOperand _ReferencedParticipationAttributeOperand = CommonalitiesLanguageCreators.commonalities.ReferencedParticipationAttributeOperand();
        final Procedure1<ReferencedParticipationAttributeOperand> _function_2 = (ReferencedParticipationAttributeOperand it_2) -> {
          ParticipationAttribute _ParticipationAttribute = CommonalitiesLanguageCreators.commonalities.ParticipationAttribute();
          final Procedure1<ParticipationAttribute> _function_3 = (ParticipationAttribute it_3) -> {
            it_3.setParticipationClass(((ParticipationClass[])Conversions.unwrapArray(CommonalitiesLanguageModelExtensions.getAllClasses(referenced.getCommonality().getParticipations().get(0)), ParticipationClass.class))[0]);
            it_3.setAttribute(CommonalitiesLanguageCreators.commonalities.languageElements.EFeatureAttribute().fromMetaclass(
              CommonalitiesLanguageCreators.commonalities.languageElements.EClassMetaclass().fromDomain(CommonalitiesLanguageCreators.commonalities.languageElements.Metamodel().forEPackage(AllElementTypesPackage.eINSTANCE)).forEClass(AllElementTypesCreators.aet.NonRoot().eClass())).forEFeature(AllElementTypesPackage.Literals.IDENTIFIED__ID));
          };
          ParticipationAttribute _doubleArrow = ObjectExtensions.<ParticipationAttribute>operator_doubleArrow(_ParticipationAttribute, _function_3);
          it_2.setParticipationAttribute(_doubleArrow);
        };
        ReferencedParticipationAttributeOperand _doubleArrow = ObjectExtensions.<ReferencedParticipationAttributeOperand>operator_doubleArrow(_ReferencedParticipationAttributeOperand, _function_2);
        _operands.add(_doubleArrow);
      };
      OperatorReferenceMapping _doubleArrow = ObjectExtensions.<OperatorReferenceMapping>operator_doubleArrow(_OperatorReferenceMapping, _function_1);
      MatcherAssert.<CommonalityReferenceMapping>assertThat(commonality.getReferences().get(0).getMappings().get(0), ModelMatchers.<CommonalityReferenceMapping>equalsDeeply(_doubleArrow, FeatureMappingReferencingTest.usingEqualsForEmfReferences(), ModelMatchers.ignoringFeatures(LanguagePackage.Literals.OPERATOR_REFERENCE_MAPPING__OPERATOR)));
    };
    this._commonalityParseHelper.inSameResourceSet(_function);
  }

  public static EqualityStrategy usingEqualsForEmfReferences() {
    return ModelMatchers.usingEqualsForReferencesTo(
      CommonalitiesLanguageCreators.commonalities.languageElements.EClassMetaclass().eClass(), 
      CommonalitiesLanguageCreators.commonalities.languageElements.EFeatureAttribute().eClass());
  }
}
