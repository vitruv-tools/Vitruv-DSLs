package tools.vitruv.dsls.commonalities.tests.util;

import com.google.common.base.Preconditions;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import tools.vitruv.dsls.commonalities.language.AttributeMappingOperand;
import tools.vitruv.dsls.commonalities.language.BidirectionalParticipationCondition;
import tools.vitruv.dsls.commonalities.language.CheckedParticipationCondition;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeMapping;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeOperand;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeReference;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.CommonalityReferenceMapping;
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.language.EnforcedParticipationCondition;
import tools.vitruv.dsls.commonalities.language.LanguageFactory;
import tools.vitruv.dsls.commonalities.language.LiteralOperand;
import tools.vitruv.dsls.commonalities.language.Operand;
import tools.vitruv.dsls.commonalities.language.OperatorAttributeMapping;
import tools.vitruv.dsls.commonalities.language.OperatorImport;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationClassOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.language.ParticipationConditionOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationPart;
import tools.vitruv.dsls.commonalities.language.ParticipationRelation;
import tools.vitruv.dsls.commonalities.language.ReferenceMappingOperand;
import tools.vitruv.dsls.commonalities.language.ReferencedParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.SimpleAttributeMapping;
import tools.vitruv.dsls.commonalities.language.SimpleReferenceMapping;
import tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass;
import tools.vitruv.dsls.commonalities.language.elements.EDataTypeClassifier;
import tools.vitruv.dsls.commonalities.language.elements.EFeatureAttribute;
import tools.vitruv.dsls.commonalities.language.elements.LanguageElementsFactory;
import tools.vitruv.dsls.commonalities.language.elements.LeastSpecificType;
import tools.vitruv.dsls.commonalities.language.elements.Metamodel;
import tools.vitruv.dsls.commonalities.language.elements.MostSpecificType;
import tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass;
import tools.vitruv.testutils.activeannotations.ModelCreators;

@ModelCreators(factory = LanguageFactory.class)
@SuppressWarnings("all")
public class CommonalitiesLanguageCreators {
  @ModelCreators(factory = LanguageElementsFactory.class)
  public static class LanguageElementsCreators {
    public static class NewEObject implements ArgumentConverter {
      public EObject convert(final Object source, final ParameterContext context) throws ArgumentConversionException {
        try {
        	return _createInstance((String) source);
        } catch (IllegalArgumentException | NullPointerException e) {
        	throw new ArgumentConversionException(e.getMessage(), e);
        }
      }
    }

    public static class Classifier implements ArgumentConverter {
      public EObject convert(final Object source, final ParameterContext context) throws ArgumentConversionException {
        try {
        	return _getClassifier((String) source);
        } catch (NullPointerException e) {
        	throw new ArgumentConversionException(e.getMessage(), e);
        }
      }
    }

    public EClassMetaclass EClassMetaclass() {
      return LanguageElementsFactory.eINSTANCE.createEClassMetaclass();
    }

    public EDataTypeClassifier EDataTypeClassifier() {
      return LanguageElementsFactory.eINSTANCE.createEDataTypeClassifier();
    }

    public Metamodel Metamodel() {
      return LanguageElementsFactory.eINSTANCE.createMetamodel();
    }

    public ResourceMetaclass ResourceMetaclass() {
      return LanguageElementsFactory.eINSTANCE.createResourceMetaclass();
    }

    public EFeatureAttribute EFeatureAttribute() {
      return LanguageElementsFactory.eINSTANCE.createEFeatureAttribute();
    }

    public MostSpecificType MostSpecificType() {
      return LanguageElementsFactory.eINSTANCE.createMostSpecificType();
    }

    public LeastSpecificType LeastSpecificType() {
      return LanguageElementsFactory.eINSTANCE.createLeastSpecificType();
    }

    private static EClassifier _getClassifier(final String classifierName) {
      return Preconditions.checkNotNull(
      	LanguageElementsFactory.eINSTANCE.getEPackage().getEClassifier(classifierName),
      	"There is no classifier called '%s' in '%s'!", classifierName, LanguageElementsFactory.eINSTANCE.getEPackage().getName()
      );
    }

    public EClassifier classifier(final String classifierName) {
      return _getClassifier(classifierName);
    }

    private static EObject _createInstance(final String className) {
      EClassifier requestedClassifier = _getClassifier(className);
      Preconditions.checkArgument(
      	requestedClassifier instanceof EClass,
      	"%s is not an EClass and can thus not be instantiated!", className
      );
      return LanguageElementsFactory.eINSTANCE.create((EClass) requestedClassifier);
    }

    public EObject create(final String className) {
      return _createInstance(className);
    }

    public <M extends EObject> M create(final Class<? extends M> clazz) {
      return clazz.cast(_createInstance(clazz.getSimpleName()));
    }
  }

  public static class NewEObject implements ArgumentConverter {
    public EObject convert(final Object source, final ParameterContext context) throws ArgumentConversionException {
      try {
      	return _createInstance((String) source);
      } catch (IllegalArgumentException | NullPointerException e) {
      	throw new ArgumentConversionException(e.getMessage(), e);
      }
    }
  }

  public static class Classifier implements ArgumentConverter {
    public EObject convert(final Object source, final ParameterContext context) throws ArgumentConversionException {
      try {
      	return _getClassifier((String) source);
      } catch (NullPointerException e) {
      	throw new ArgumentConversionException(e.getMessage(), e);
      }
    }
  }

  public static final CommonalitiesLanguageCreators commonalities = new CommonalitiesLanguageCreators();

  public final CommonalitiesLanguageCreators.LanguageElementsCreators languageElements = new CommonalitiesLanguageCreators.LanguageElementsCreators();

  public CommonalityFile CommonalityFile() {
    return LanguageFactory.eINSTANCE.createCommonalityFile();
  }

  public OperatorImport OperatorImport() {
    return LanguageFactory.eINSTANCE.createOperatorImport();
  }

  public Concept Concept() {
    return LanguageFactory.eINSTANCE.createConcept();
  }

  public Commonality Commonality() {
    return LanguageFactory.eINSTANCE.createCommonality();
  }

  public Participation Participation() {
    return LanguageFactory.eINSTANCE.createParticipation();
  }

  public ParticipationClass ParticipationClass() {
    return LanguageFactory.eINSTANCE.createParticipationClass();
  }

  public ParticipationPart ParticipationPart() {
    return LanguageFactory.eINSTANCE.createParticipationPart();
  }

  public ParticipationRelation ParticipationRelation() {
    return LanguageFactory.eINSTANCE.createParticipationRelation();
  }

  public ParticipationCondition ParticipationCondition() {
    return LanguageFactory.eINSTANCE.createParticipationCondition();
  }

  public ParticipationConditionOperand ParticipationConditionOperand() {
    return LanguageFactory.eINSTANCE.createParticipationConditionOperand();
  }

  public CommonalityAttribute CommonalityAttribute() {
    return LanguageFactory.eINSTANCE.createCommonalityAttribute();
  }

  public CommonalityAttributeMapping CommonalityAttributeMapping() {
    return LanguageFactory.eINSTANCE.createCommonalityAttributeMapping();
  }

  public SimpleAttributeMapping SimpleAttributeMapping() {
    return LanguageFactory.eINSTANCE.createSimpleAttributeMapping();
  }

  public OperatorAttributeMapping OperatorAttributeMapping() {
    return LanguageFactory.eINSTANCE.createOperatorAttributeMapping();
  }

  public AttributeMappingOperand AttributeMappingOperand() {
    return LanguageFactory.eINSTANCE.createAttributeMappingOperand();
  }

  public ParticipationAttribute ParticipationAttribute() {
    return LanguageFactory.eINSTANCE.createParticipationAttribute();
  }

  public CommonalityAttributeReference CommonalityAttributeReference() {
    return LanguageFactory.eINSTANCE.createCommonalityAttributeReference();
  }

  public CommonalityReference CommonalityReference() {
    return LanguageFactory.eINSTANCE.createCommonalityReference();
  }

  public CommonalityReferenceMapping CommonalityReferenceMapping() {
    return LanguageFactory.eINSTANCE.createCommonalityReferenceMapping();
  }

  public SimpleReferenceMapping SimpleReferenceMapping() {
    return LanguageFactory.eINSTANCE.createSimpleReferenceMapping();
  }

  public OperatorReferenceMapping OperatorReferenceMapping() {
    return LanguageFactory.eINSTANCE.createOperatorReferenceMapping();
  }

  public ReferenceMappingOperand ReferenceMappingOperand() {
    return LanguageFactory.eINSTANCE.createReferenceMappingOperand();
  }

  public ReferencedParticipationAttributeOperand ReferencedParticipationAttributeOperand() {
    return LanguageFactory.eINSTANCE.createReferencedParticipationAttributeOperand();
  }

  public Operand Operand() {
    return LanguageFactory.eINSTANCE.createOperand();
  }

  public LiteralOperand LiteralOperand() {
    return LanguageFactory.eINSTANCE.createLiteralOperand();
  }

  public ParticipationClassOperand ParticipationClassOperand() {
    return LanguageFactory.eINSTANCE.createParticipationClassOperand();
  }

  public ParticipationAttributeOperand ParticipationAttributeOperand() {
    return LanguageFactory.eINSTANCE.createParticipationAttributeOperand();
  }

  public CommonalityAttributeOperand CommonalityAttributeOperand() {
    return LanguageFactory.eINSTANCE.createCommonalityAttributeOperand();
  }

  public BidirectionalParticipationCondition BidirectionalParticipationCondition() {
    return LanguageFactory.eINSTANCE.createBidirectionalParticipationCondition();
  }

  public EnforcedParticipationCondition EnforcedParticipationCondition() {
    return LanguageFactory.eINSTANCE.createEnforcedParticipationCondition();
  }

  public CheckedParticipationCondition CheckedParticipationCondition() {
    return LanguageFactory.eINSTANCE.createCheckedParticipationCondition();
  }

  private static EClassifier _getClassifier(final String classifierName) {
    return Preconditions.checkNotNull(
    	LanguageFactory.eINSTANCE.getEPackage().getEClassifier(classifierName),
    	"There is no classifier called '%s' in '%s'!", classifierName, LanguageFactory.eINSTANCE.getEPackage().getName()
    );
  }

  public EClassifier classifier(final String classifierName) {
    return _getClassifier(classifierName);
  }

  private static EObject _createInstance(final String className) {
    EClassifier requestedClassifier = _getClassifier(className);
    Preconditions.checkArgument(
    	requestedClassifier instanceof EClass,
    	"%s is not an EClass and can thus not be instantiated!", className
    );
    return LanguageFactory.eINSTANCE.create((EClass) requestedClassifier);
  }

  public EObject create(final String className) {
    return _createInstance(className);
  }

  public <M extends EObject> M create(final Class<? extends M> clazz) {
    return clazz.cast(_createInstance(clazz.getSimpleName()));
  }
}
