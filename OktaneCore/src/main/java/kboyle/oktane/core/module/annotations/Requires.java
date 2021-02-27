package kboyle.oktane.core.module.annotations;

import kboyle.oktane.core.module.Precondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Requires {
    Class<? extends Precondition> precondition();
    String[] arguments() default {};
}
