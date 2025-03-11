package tools.vitruv.dsls.commonalities.runtime.operators.participation.relation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.eclipse.xtend.lib.macro.Active;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Active(ParticipationRelationOperatorProcessor.class)
@SuppressWarnings("all")
public @interface ParticipationRelationOperator {
  public String name();
}
