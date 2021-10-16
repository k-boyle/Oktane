package com.github.kboyle.oktane.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface OtkaneApplication {
    boolean compileTimeValidation() default false;
}
