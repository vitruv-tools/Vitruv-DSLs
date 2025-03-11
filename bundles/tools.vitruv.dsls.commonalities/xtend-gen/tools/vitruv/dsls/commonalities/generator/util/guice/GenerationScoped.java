package tools.vitruv.dsls.commonalities.generator.util.guice;

import com.google.inject.ScopeAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Types annotated with this scope annotation will be instantiated once per
 * generator execution for one input resource.
 */
@ScopeAnnotation
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SuppressWarnings("all")
public @interface GenerationScoped {
}
