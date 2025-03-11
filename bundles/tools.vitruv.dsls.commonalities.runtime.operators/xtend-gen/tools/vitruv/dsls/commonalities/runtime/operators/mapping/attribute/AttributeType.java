package tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("all")
public @interface AttributeType {
  public boolean multiValued();
  public Class<?> type();
}
