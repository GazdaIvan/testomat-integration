package io.testomat.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a custom title for a test method, used in reports or external systems.
 * <p>
 * If not provided, the test method name will be used as a fallback.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Title {
    String value();
}
