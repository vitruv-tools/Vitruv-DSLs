package tools.vitruv.dsls.commonalities.tests.operators;

import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AbstractAttributeMappingOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeMappingOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeType;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * Converts between an integer number on the participation side and a list of
 * its decimal digits on the commonality side.
 * <p>
 * <code>null</code> or an empty list on one side is converted to
 * <code>0</code> or an empty list on the respectively other side.
 */
@AttributeMappingOperator(name = "digits", commonalityAttributeType = @AttributeType(multiValued = true, type = Integer.class), participationAttributeType = @AttributeType(multiValued = false, type = Integer.class))
@SuppressWarnings("all")
public class DigitsAttributeOperator extends AbstractAttributeMappingOperator<List<Integer>, Integer> {
  public DigitsAttributeOperator(final ReactionExecutionState executionState) {
    super(executionState);
  }

  @Override
  public List<Integer> applyTowardsCommonality(final Integer participationAttributeValue) {
    if ((participationAttributeValue == null)) {
      return Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList());
    }
    final String numberString = participationAttributeValue.toString();
    final IntUnaryOperator _function = (int it) -> {
      return Character.getNumericValue(it);
    };
    return (List<Integer>)Conversions.doWrapArray(numberString.chars().map(_function).toArray());
  }

  @Override
  public Integer applyTowardsParticipation(final List<Integer> commonalityAttributeValue) {
    boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(commonalityAttributeValue);
    if (_isNullOrEmpty) {
      return Integer.valueOf(0);
    }
    return Integer.valueOf(Integer.parseInt(IterableExtensions.join(commonalityAttributeValue)));
  }
}
