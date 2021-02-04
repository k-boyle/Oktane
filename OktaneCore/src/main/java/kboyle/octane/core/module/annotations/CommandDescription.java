package kboyle.octane.core.module.annotations;

import kboyle.octane.core.module.Precondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to declare a method as a command, and append any extra metadata.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandDescription {
    /**
     * @return The name of the command.
     */
    String name() default "";

    /**
     * @return The aliases of the command.
     */
    String[] aliases();

    /**
     * @return The description of the command.
     */
    String description() default "";

    /**
     * @return The preconditions of the command.
     */
    Class<? extends Precondition>[] preconditions() default {};

    /**
     * @return Whether or not the command is synchronised.
     */
    boolean synchronised() default false;
}
