package com.github.kboyle.oktane.core.annotation;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Aliases {
    String[] value() default {};
}
