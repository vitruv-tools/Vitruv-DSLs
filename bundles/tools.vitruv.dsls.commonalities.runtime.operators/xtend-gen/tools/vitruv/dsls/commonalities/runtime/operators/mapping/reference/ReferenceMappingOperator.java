package tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.eclipse.xtend.lib.macro.Active;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Active(ReferenceMappingOperatorProcessor.class)
@SuppressWarnings("all")
public @interface ReferenceMappingOperator {
  public String name();
  public boolean isMultiValued();
  public boolean isAttributeReference() default false;
}
