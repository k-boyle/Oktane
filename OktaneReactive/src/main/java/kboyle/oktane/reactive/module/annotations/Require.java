package kboyle.oktane.reactive.module.annotations;

import kboyle.oktane.reactive.module.Precondition;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Repeatable(Requires.class)
public @interface Require {
    Class<? extends Precondition> precondition();
    String[] arguments() default {};
}
