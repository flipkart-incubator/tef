package flipkart.tef.guicebridge;


import com.google.inject.ScopeAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Scope annotation for Guice to Tef bridge
 * This annotation solves 2 use-cases
 * <p>
 * 1. Bind the custom scope `TefGuiceScope`
 * 2. Marker annotation to be used on the classes where @InjectData needs to be powered by guice
 * Date: 1/06/22
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface TefRequestScoped {
}
