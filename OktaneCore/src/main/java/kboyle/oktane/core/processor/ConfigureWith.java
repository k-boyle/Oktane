package kboyle.oktane.core.processor;

import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.parsers.TypeParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a class (e.g. a {@link TypeParser} to be added automatically to a {@link CommandHandler.Builder}.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ConfigureWith {
    /**
     * The priority to give the configurator, default 0 (by default applied after stock configurators)
     */
    int priority() default 0;
}
