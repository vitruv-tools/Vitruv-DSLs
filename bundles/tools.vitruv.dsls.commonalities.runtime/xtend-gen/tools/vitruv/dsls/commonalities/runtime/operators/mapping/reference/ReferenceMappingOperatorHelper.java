package tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.xtend2.lib.StringConcatenation;

@Utility
@SuppressWarnings("all")
public final class ReferenceMappingOperatorHelper {
  private static ReferenceMappingOperator getAnnotation(final IReferenceMappingOperator operator) {
    final ReferenceMappingOperator annotation = operator.getClass().<ReferenceMappingOperator>getAnnotation(ReferenceMappingOperator.class);
    if ((annotation == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Missing operator annotation for reference mapping operator: ");
      String _name = operator.getClass().getName();
      _builder.append(_name);
      throw new IllegalStateException(_builder.toString());
    }
    return annotation;
  }

  public static String getName(final IReferenceMappingOperator operator) {
    return ReferenceMappingOperatorHelper.getAnnotation(operator).name();
  }

  public static boolean isMultiValued(final IReferenceMappingOperator operator) {
    return ReferenceMappingOperatorHelper.getAnnotation(operator).isMultiValued();
  }

  public static boolean isAttributeReference(final IReferenceMappingOperator operator) {
    return ReferenceMappingOperatorHelper.getAnnotation(operator).isAttributeReference();
  }

  private ReferenceMappingOperatorHelper() {
    
  }
}
