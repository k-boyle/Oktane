package com.github.kboyle.oktane.core.annotation;

import com.github.kboyle.oktane.core.parsing.TypeParser;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface OverrideTypeParser {
    Class<TypeParser<?>> value();
}
