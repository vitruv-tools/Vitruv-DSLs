package tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute;

/**
 * @param <C> The commonality attribute type
 * @param <P> The participation attribute type
 */
@SuppressWarnings("all")
public interface IAttributeMappingOperator<C extends Object, P extends Object> {
  /**
   * Applies this operator in the direction towards the commonality.
   * 
   * @param participationAttributeValue
   * 			the participation attribute value, or <code>null</code> if the
   * 			mapping did not specify any participation attribute operand
   * @return the result value
   */
  C applyTowardsCommonality(final P participationAttributeValue);

  /**
   * Applies this operator in the direction towards the commonality.
   * 
   * @param commonalityAttributeValue
   * 			the commonality attribute value
   * @return the result value
   */
  P applyTowardsParticipation(final C commonalityAttributeValue);
}
