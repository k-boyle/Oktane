package kboyle.oktane.core.module.annotations;

import kboyle.oktane.core.parsers.ArgumentParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface OverrideArgumentParser {
    Class<? extends ArgumentParser> value();
}
