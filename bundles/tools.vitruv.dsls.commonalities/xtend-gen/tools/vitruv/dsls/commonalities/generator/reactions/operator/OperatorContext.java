package tools.vitruv.dsls.commonalities.generator.reactions.operator;

import org.eclipse.xtext.xbase.XExpression;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.runtime.operators.AttributeOperand;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public interface OperatorContext {
  /**
   * Gets the {@link TypeProvider}.
   */
  TypeProvider getTypeProvider();

  /**
   * Whether to pass attribute values instead of {@link AttributeOperand}
   * instances for participation attribute operands.
   * <p>
   * May throw an {@link UnsupportedOperationException} if the context does
   * not support participation attribute operands.
   */
  boolean passParticipationAttributeValues();

  /**
   * Whether to pass attribute values instead of {@link AttributeOperand}
   * instances for commonality attribute operands.
   * <p>
   * May throw an {@link UnsupportedOperationException} if the context does
   * not support commonality attribute operands.
   */
  boolean passCommonalityAttributeValues();

  /**
   * Gets the expression for accessing the involved commonality instance.
   * <p>
   * May throw an {@link UnsupportedOperationException} if the context does
   * not support operands that require access to the commonality instance.
   */
  XExpression getIntermediate();

  /**
   * Gets the expression for accessing the participation object for the
   * given participation class.
   * <p>
   * May throw an {@link UnsupportedOperationException} if the context does
   * not support operands that require access to participation objects.
   */
  XExpression getParticipationObject(final ParticipationClass participationClass);
}
