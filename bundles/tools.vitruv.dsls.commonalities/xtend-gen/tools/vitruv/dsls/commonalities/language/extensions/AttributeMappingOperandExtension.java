package tools.vitruv.dsls.commonalities.language.extensions;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import tools.vitruv.dsls.commonalities.language.AttributeMappingOperand;
import tools.vitruv.dsls.commonalities.language.OperatorAttributeMapping;

@Utility
@SuppressWarnings("all")
final class AttributeMappingOperandExtension {
  public static boolean isInAttributeMappingContext(final AttributeMappingOperand operand) {
    OperatorAttributeMapping _attributeMapping = AttributeMappingOperandExtension.getAttributeMapping(operand);
    return (_attributeMapping != null);
  }

  public static OperatorAttributeMapping getAttributeMapping(final AttributeMappingOperand operand) {
    return CommonalitiesLanguageElementExtension.<OperatorAttributeMapping>getOptionalDirectEContainer(operand, OperatorAttributeMapping.class);
  }

  private AttributeMappingOperandExtension() {
    
  }
}
