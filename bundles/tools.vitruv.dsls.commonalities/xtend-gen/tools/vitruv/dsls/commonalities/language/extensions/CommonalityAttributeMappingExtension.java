package tools.vitruv.dsls.commonalities.language.extensions;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import tools.vitruv.dsls.commonalities.language.AttributeMappingOperand;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeMapping;
import tools.vitruv.dsls.commonalities.language.OperatorAttributeMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.SimpleAttributeMapping;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider;
import tools.vitruv.dsls.commonalities.language.elements.Domain;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;

@Utility
@SuppressWarnings("all")
final class CommonalityAttributeMappingExtension {
  public static boolean isSimpleMapping(final CommonalityAttributeMapping mapping) {
    return (mapping instanceof SimpleAttributeMapping);
  }

  public static boolean isOperatorMapping(final CommonalityAttributeMapping mapping) {
    return (mapping instanceof OperatorAttributeMapping);
  }

  protected static ParticipationAttribute _getParticipationAttribute(final SimpleAttributeMapping mapping) {
    return mapping.getAttribute();
  }

  protected static ParticipationAttribute _getParticipationAttribute(final OperatorAttributeMapping mapping) {
    ParticipationAttributeOperand _participationAttributeOperand = OperatorAttributeMappingExtension.getParticipationAttributeOperand(mapping);
    ParticipationAttribute _participationAttribute = null;
    if (_participationAttributeOperand!=null) {
      _participationAttribute=_participationAttributeOperand.getParticipationAttribute();
    }
    return _participationAttribute;
  }

  protected static Collection<ParticipationClass> _getInvolvedParticipationClasses(final SimpleAttributeMapping mapping) {
    return List.<ParticipationClass>of(mapping.getAttribute().getParticipationClass());
  }

  protected static Collection<ParticipationClass> _getInvolvedParticipationClasses(final OperatorAttributeMapping mapping) {
    final Function1<AttributeMappingOperand, ParticipationClass> _function = (AttributeMappingOperand it) -> {
      return OperandExtension.getParticipationClass(it);
    };
    return IterableExtensions.<ParticipationClass>toSet(IterableExtensions.<ParticipationClass>filterNull(ListExtensions.<AttributeMappingOperand, ParticipationClass>map(mapping.getOperands(), _function)));
  }

  protected static Participation _getParticipation(final SimpleAttributeMapping mapping) {
    return ParticipationClassExtension.getParticipation(mapping.getAttribute().getParticipationClass());
  }

  protected static Participation _getParticipation(final OperatorAttributeMapping mapping) {
    final ParticipationAttribute participationAttribute = CommonalityAttributeMappingExtension.getParticipationAttribute(mapping);
    Participation _xifexpression = null;
    if ((participationAttribute != null)) {
      _xifexpression = ParticipationClassExtension.getParticipation(participationAttribute.getParticipationClass());
    } else {
      Participation _xblockexpression = null;
      {
        boolean _isEmpty = IterableExtensions.isEmpty(OperatorAttributeMappingExtension.getParticipationClassOperands(mapping));
        boolean _not = (!_isEmpty);
        XtendAssertHelper.assertTrue(_not);
        _xblockexpression = ParticipationClassExtension.getParticipation(IterableExtensions.<ParticipationClass>head(OperatorAttributeMappingExtension.getParticipationClassOperands(mapping)));
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }

  public static CommonalityAttribute getDeclaringAttribute(final CommonalityAttributeMapping mapping) {
    return CommonalitiesLanguageElementExtension.<CommonalityAttribute>getDirectEContainer(mapping, CommonalityAttribute.class);
  }

  /**
   * Returns <code>true<code> if the commonality side of the mapping is
   * multi-valued.
   */
  protected static boolean _isMultiValuedRead(final SimpleAttributeMapping mapping) {
    return mapping.getAttribute().isMultiValued();
  }

  protected static boolean _isMultiValuedRead(final OperatorAttributeMapping mapping) {
    final AttributeTypeDescription typeDescription = AttributeMappingExtension.getCommonalityAttributeTypeDescription(mapping);
    boolean _xifexpression = false;
    if ((typeDescription != null)) {
      _xifexpression = typeDescription.isMultiValued();
    } else {
      _xifexpression = false;
    }
    return _xifexpression;
  }

  /**
   * Returns <code>true<code> if the participation side of the mapping is
   * multi-valued.
   */
  protected static boolean _isMultiValuedWrite(final SimpleAttributeMapping mapping) {
    return mapping.getAttribute().isMultiValued();
  }

  protected static boolean _isMultiValuedWrite(final OperatorAttributeMapping mapping) {
    final AttributeTypeDescription typeDescription = AttributeMappingExtension.getParticipationAttributeTypeDescription(mapping);
    boolean _xifexpression = false;
    if ((typeDescription != null)) {
      _xifexpression = typeDescription.isMultiValued();
    } else {
      _xifexpression = false;
    }
    return _xifexpression;
  }

  protected static Classifier _getCommonalityAttributeType(final SimpleAttributeMapping mapping) {
    return mapping.getAttribute().getType();
  }

  protected static Classifier _getCommonalityAttributeType(final OperatorAttributeMapping mapping) {
    Participation _participation = CommonalityAttributeMappingExtension.getParticipation(mapping);
    Domain _domain = null;
    if (_participation!=null) {
      _domain=ParticipationExtension.getDomain(_participation);
    }
    final Domain domain = _domain;
    final AttributeTypeDescription attributeTypeDescription = AttributeMappingExtension.getCommonalityAttributeTypeDescription(mapping);
    String _qualifiedTypeName = null;
    if (attributeTypeDescription!=null) {
      _qualifiedTypeName=attributeTypeDescription.getQualifiedTypeName();
    }
    return ClassifierProvider.INSTANCE.findClassifier(domain, _qualifiedTypeName);
  }

  protected static Classifier _getParticipationAttributeType(final SimpleAttributeMapping mapping) {
    return mapping.getAttribute().getType();
  }

  protected static Classifier _getParticipationAttributeType(final OperatorAttributeMapping mapping) {
    Participation _participation = CommonalityAttributeMappingExtension.getParticipation(mapping);
    Domain _domain = null;
    if (_participation!=null) {
      _domain=ParticipationExtension.getDomain(_participation);
    }
    final Domain domain = _domain;
    final AttributeTypeDescription attributeTypeDescription = AttributeMappingExtension.getParticipationAttributeTypeDescription(mapping);
    String _qualifiedTypeName = null;
    if (attributeTypeDescription!=null) {
      _qualifiedTypeName=attributeTypeDescription.getQualifiedTypeName();
    }
    return ClassifierProvider.INSTANCE.findClassifier(domain, _qualifiedTypeName);
  }

  public static Classifier getProvidedType(final CommonalityAttributeMapping mapping) {
    Classifier _xifexpression = null;
    boolean _isRead = mapping.isRead();
    if (_isRead) {
      _xifexpression = CommonalityAttributeMappingExtension.getCommonalityAttributeType(mapping);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  public static Classifier getRequiredType(final CommonalityAttributeMapping mapping) {
    Classifier _xifexpression = null;
    boolean _isWrite = mapping.isWrite();
    if (_isWrite) {
      _xifexpression = CommonalityAttributeMappingExtension.getCommonalityAttributeType(mapping);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  public static ParticipationAttribute getParticipationAttribute(final CommonalityAttributeMapping mapping) {
    if (mapping instanceof OperatorAttributeMapping) {
      return _getParticipationAttribute((OperatorAttributeMapping)mapping);
    } else if (mapping instanceof SimpleAttributeMapping) {
      return _getParticipationAttribute((SimpleAttributeMapping)mapping);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping).toString());
    }
  }

  public static Collection<ParticipationClass> getInvolvedParticipationClasses(final CommonalityAttributeMapping mapping) {
    if (mapping instanceof OperatorAttributeMapping) {
      return _getInvolvedParticipationClasses((OperatorAttributeMapping)mapping);
    } else if (mapping instanceof SimpleAttributeMapping) {
      return _getInvolvedParticipationClasses((SimpleAttributeMapping)mapping);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping).toString());
    }
  }

  public static Participation getParticipation(final CommonalityAttributeMapping mapping) {
    if (mapping instanceof OperatorAttributeMapping) {
      return _getParticipation((OperatorAttributeMapping)mapping);
    } else if (mapping instanceof SimpleAttributeMapping) {
      return _getParticipation((SimpleAttributeMapping)mapping);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping).toString());
    }
  }

  public static boolean isMultiValuedRead(final CommonalityAttributeMapping mapping) {
    if (mapping instanceof OperatorAttributeMapping) {
      return _isMultiValuedRead((OperatorAttributeMapping)mapping);
    } else if (mapping instanceof SimpleAttributeMapping) {
      return _isMultiValuedRead((SimpleAttributeMapping)mapping);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping).toString());
    }
  }

  public static boolean isMultiValuedWrite(final CommonalityAttributeMapping mapping) {
    if (mapping instanceof OperatorAttributeMapping) {
      return _isMultiValuedWrite((OperatorAttributeMapping)mapping);
    } else if (mapping instanceof SimpleAttributeMapping) {
      return _isMultiValuedWrite((SimpleAttributeMapping)mapping);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping).toString());
    }
  }

  public static Classifier getCommonalityAttributeType(final CommonalityAttributeMapping mapping) {
    if (mapping instanceof OperatorAttributeMapping) {
      return _getCommonalityAttributeType((OperatorAttributeMapping)mapping);
    } else if (mapping instanceof SimpleAttributeMapping) {
      return _getCommonalityAttributeType((SimpleAttributeMapping)mapping);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping).toString());
    }
  }

  public static Classifier getParticipationAttributeType(final CommonalityAttributeMapping mapping) {
    if (mapping instanceof OperatorAttributeMapping) {
      return _getParticipationAttributeType((OperatorAttributeMapping)mapping);
    } else if (mapping instanceof SimpleAttributeMapping) {
      return _getParticipationAttributeType((SimpleAttributeMapping)mapping);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping).toString());
    }
  }

  private CommonalityAttributeMappingExtension() {
    
  }
}
