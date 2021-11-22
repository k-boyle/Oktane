package com.github.kboyle.oktane.core.configuration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@SpringBootApplication
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(OktaneConfiguration.class)
public @interface OktaneApplication {
}
