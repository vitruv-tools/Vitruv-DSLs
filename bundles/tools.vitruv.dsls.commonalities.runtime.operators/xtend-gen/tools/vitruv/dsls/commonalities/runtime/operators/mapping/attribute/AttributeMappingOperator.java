package tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.eclipse.xtend.lib.macro.Active;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Active(AttributeMappingOperatorProcessor.class)
@SuppressWarnings("all")
public @interface AttributeMappingOperator {
  public String name();
  public AttributeType commonalityAttributeType();
  public AttributeType participationAttributeType();
}
