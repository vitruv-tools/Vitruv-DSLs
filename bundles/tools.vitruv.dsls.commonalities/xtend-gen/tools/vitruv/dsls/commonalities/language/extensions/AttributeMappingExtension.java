package tools.vitruv.dsls.commonalities.language.extensions;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.OperatorAttributeMapping;
import tools.vitruv.dsls.commonalities.runtime.operators.CommonalitiesOperatorConventions;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeMappingOperator;
import tools.vitruv.dsls.commonalities.util.JvmAnnotationHelper;

@Utility
@SuppressWarnings("all")
final class AttributeMappingExtension {
  private static JvmAnnotationReference getAttributeMappingOperatorAnnotation(final JvmDeclaredType operatorType) {
    final Function1<JvmAnnotationReference, Boolean> _function = (JvmAnnotationReference it) -> {
      String _qualifiedName = it.getAnnotation().getQualifiedName();
      String _name = AttributeMappingOperator.class.getName();
      return Boolean.valueOf(Objects.equal(_qualifiedName, _name));
    };
    return IterableExtensions.<JvmAnnotationReference>head(IterableExtensions.<JvmAnnotationReference>filter(operatorType.getAnnotations(), _function));
  }

  public static String getAttributeMappingOperatorName(final JvmDeclaredType operatorType) {
    return CommonalitiesOperatorConventions.toOperatorLanguageName(operatorType.getSimpleName());
  }

  public static String getName(final OperatorAttributeMapping mapping) {
    return AttributeMappingExtension.getAttributeMappingOperatorName(mapping.getOperator());
  }

  private static AttributeTypeDescription getAttributeTypeDescription(final JvmDeclaredType operator, final String valueName) {
    final JvmAnnotationReference annotation = AttributeMappingExtension.getAttributeMappingOperatorAnnotation(operator);
    if ((annotation == null)) {
      return null;
    }
    final JvmAnnotationReference typeAnnotation = JvmAnnotationHelper.getAnnotationAnnotationValue(annotation, valueName);
    final boolean multiValued = JvmAnnotationHelper.getBooleanAnnotationValue(typeAnnotation, "multiValued");
    final JvmTypeReference typeRef = JvmAnnotationHelper.getTypeAnnotationValue(typeAnnotation, "type");
    String _qualifiedName = typeRef.getQualifiedName();
    return new AttributeTypeDescription(multiValued, _qualifiedName);
  }

  public static AttributeTypeDescription getCommonalityAttributeTypeDescription(final OperatorAttributeMapping mapping) {
    JvmDeclaredType _operator = mapping.getOperator();
    AttributeTypeDescription _attributeTypeDescription = null;
    if (_operator!=null) {
      _attributeTypeDescription=AttributeMappingExtension.getAttributeTypeDescription(_operator, "commonalityAttributeType");
    }
    return _attributeTypeDescription;
  }

  public static AttributeTypeDescription getParticipationAttributeTypeDescription(final OperatorAttributeMapping mapping) {
    JvmDeclaredType _operator = mapping.getOperator();
    AttributeTypeDescription _attributeTypeDescription = null;
    if (_operator!=null) {
      _attributeTypeDescription=AttributeMappingExtension.getAttributeTypeDescription(_operator, "participationAttributeType");
    }
    return _attributeTypeDescription;
  }

  private AttributeMappingExtension() {
    
  }
}
