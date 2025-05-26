package io.testomat.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Associates a test method with an external Testomat.io test case ID.
 * <p>
 * This ID is used to link the test execution with a specific test case in the Testomat.io system.
 * It should match the test ID as defined in your Testomat project.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TestId {
    String value();
}

