package tools.vitruv.dsls.commonalities.language.extensions;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Set;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.LiteralOperand;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ReferencedParticipationAttributeOperand;

@Utility
@SuppressWarnings("all")
final class OperatorReferenceMappingExtension {
  public static Iterable<ParticipationAttribute> getReferencedParticipationAttributes(final OperatorReferenceMapping mapping) {
    final Function1<ReferencedParticipationAttributeOperand, ParticipationAttribute> _function = (ReferencedParticipationAttributeOperand it) -> {
      return it.getParticipationAttribute();
    };
    return IterableExtensions.<ReferencedParticipationAttributeOperand, ParticipationAttribute>map(Iterables.<ReferencedParticipationAttributeOperand>filter(mapping.getOperands(), ReferencedParticipationAttributeOperand.class), _function);
  }

  public static Set<ParticipationClass> getReferencedParticipationClasses(final OperatorReferenceMapping mapping) {
    final Function1<ParticipationAttribute, ParticipationClass> _function = (ParticipationAttribute it) -> {
      return it.getParticipationClass();
    };
    return IterableExtensions.<ParticipationClass>toSet(IterableExtensions.<ParticipationAttribute, ParticipationClass>map(OperatorReferenceMappingExtension.getReferencedParticipationAttributes(mapping), _function));
  }

  public static Iterable<LiteralOperand> getPassedOperands(final OperatorReferenceMapping mapping) {
    return Iterables.<LiteralOperand>filter(mapping.getOperands(), LiteralOperand.class);
  }

  private OperatorReferenceMappingExtension() {
    
  }
}
