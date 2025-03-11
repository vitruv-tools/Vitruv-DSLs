package tools.vitruv.dsls.commonalities.language.extensions;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.ReferenceMappingOperand;

@Utility
@SuppressWarnings("all")
final class ReferenceMappingOperandExtension {
  public static boolean isInReferenceMappingContext(final ReferenceMappingOperand operand) {
    OperatorReferenceMapping _referenceMapping = ReferenceMappingOperandExtension.getReferenceMapping(operand);
    return (_referenceMapping != null);
  }

  public static OperatorReferenceMapping getReferenceMapping(final ReferenceMappingOperand operand) {
    return CommonalitiesLanguageElementExtension.<OperatorReferenceMapping>getOptionalDirectEContainer(operand, OperatorReferenceMapping.class);
  }

  private ReferenceMappingOperandExtension() {
    
  }
}
