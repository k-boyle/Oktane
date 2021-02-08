package kboyle.oktane.core.module.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to add extra metadata to a parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ParameterDescription {
    /**
     * @return The name of the parameter.
     */
    String name() default "";

    /**
     * @return The description of the parameter.
     */
    String description() default "";

    /**
     * @return Whether the parameter is a remainder or not.
     */
    boolean remainder() default false;
}
