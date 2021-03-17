package kboyle.oktane.core.module.annotations;

import kboyle.oktane.core.module.Precondition;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Repeatable(Requires.class)
public @interface Require {
    Class<? extends Precondition> precondition();
    String[] arguments() default {};
}
