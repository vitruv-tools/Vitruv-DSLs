package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.eclipse.xtend.lib.macro.Active;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Active(ParticipationConditionOperatorProcessor.class)
@SuppressWarnings("all")
public @interface ParticipationConditionOperator {
  public String name();
}
