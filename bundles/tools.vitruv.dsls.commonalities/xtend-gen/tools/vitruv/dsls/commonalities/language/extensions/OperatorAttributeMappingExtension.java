package tools.vitruv.dsls.commonalities.language.extensions;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.AttributeMappingOperand;
import tools.vitruv.dsls.commonalities.language.OperatorAttributeMapping;
import tools.vitruv.dsls.commonalities.language.ParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;

@Utility
@SuppressWarnings("all")
final class OperatorAttributeMappingExtension {
  public static ParticipationAttributeOperand getParticipationAttributeOperand(final OperatorAttributeMapping mapping) {
    return IterableExtensions.<ParticipationAttributeOperand>head(Iterables.<ParticipationAttributeOperand>filter(mapping.getOperands(), ParticipationAttributeOperand.class));
  }

  public static Iterable<ParticipationClass> getParticipationClassOperands(final OperatorAttributeMapping mapping) {
    return Iterables.<ParticipationClass>filter(mapping.getOperands(), ParticipationClass.class);
  }

  /**
   * Gets all operands that are common for both application directions (from participation to commonality and from
   * commonality to participation).
   * <p>
   * I.e. this omits the participation attribute operand (if it is present).
   */
  public static Iterable<AttributeMappingOperand> getCommonOperands(final OperatorAttributeMapping mapping) {
    final Function1<AttributeMappingOperand, Boolean> _function = (AttributeMappingOperand operand) -> {
      return Boolean.valueOf((!(operand instanceof ParticipationAttributeOperand)));
    };
    return IterableExtensions.<AttributeMappingOperand>filter(mapping.getOperands(), _function);
  }

  private OperatorAttributeMappingExtension() {
    
  }
}
