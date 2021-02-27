package kboyle.oktane.core.module.annotations;

import kboyle.oktane.core.module.Precondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds extra metadata to a module.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleDescription {
    /**
     * @return The name of the module.
     */
    String name() default "";

    /**
     * @return The groups of the module.
     */
    String[] groups() default {};

    /**
     * @return The description of the module.
     */
    String description() default "";

    /**
     * @return The preconditions of the module.
     */
    @Deprecated
    Class<? extends Precondition>[] preconditions() default {};

    /**
     * @return Whether or not the module is a singleton.
     */
    @Deprecated
    boolean singleton() default false;

    /**
     * @return Whether or not the module is synchronised.
     */
    @Deprecated
    boolean synchronised() default false;
}
