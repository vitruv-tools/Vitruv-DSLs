package tools.vitruv.dsls.commonalities.language.extensions;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.runtime.operators.CommonalitiesOperatorConventions;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.ReferenceMappingOperator;
import tools.vitruv.dsls.commonalities.util.JvmAnnotationHelper;

@Utility
@SuppressWarnings("all")
final class ReferenceMappingExtension {
  private static JvmAnnotationReference getReferenceMappingOperatorAnnotation(final JvmDeclaredType operatorType) {
    final Function1<JvmAnnotationReference, Boolean> _function = (JvmAnnotationReference it) -> {
      String _qualifiedName = it.getAnnotation().getQualifiedName();
      String _name = ReferenceMappingOperator.class.getName();
      return Boolean.valueOf(Objects.equal(_qualifiedName, _name));
    };
    return IterableExtensions.<JvmAnnotationReference>head(IterableExtensions.<JvmAnnotationReference>filter(operatorType.getAnnotations(), _function));
  }

  public static String getReferenceMappingOperatorName(final JvmDeclaredType operatorType) {
    return CommonalitiesOperatorConventions.toOperatorLanguageName(operatorType.getSimpleName());
  }

  public static String getName(final OperatorReferenceMapping mapping) {
    return ReferenceMappingExtension.getReferenceMappingOperatorName(mapping.getOperator());
  }

  public static boolean isMultiValued(final OperatorReferenceMapping mapping) {
    boolean _xblockexpression = false;
    {
      final JvmAnnotationReference annotation = ReferenceMappingExtension.getReferenceMappingOperatorAnnotation(mapping.getOperator());
      if ((annotation == null)) {
        return false;
      }
      _xblockexpression = JvmAnnotationHelper.getBooleanAnnotationValue(annotation, "isMultiValued");
    }
    return _xblockexpression;
  }

  public static boolean isAttributeReference(final OperatorReferenceMapping mapping) {
    boolean _xblockexpression = false;
    {
      final JvmAnnotationReference annotation = ReferenceMappingExtension.getReferenceMappingOperatorAnnotation(mapping.getOperator());
      if ((annotation == null)) {
        return false;
      }
      _xblockexpression = JvmAnnotationHelper.getBooleanAnnotationValue(annotation, "isAttributeReference");
    }
    return _xblockexpression;
  }

  private ReferenceMappingExtension() {
    
  }
}
