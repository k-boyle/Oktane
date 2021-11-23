package com.github.kboyle.oktane.core.configuration;

import com.github.kboyle.oktane.core.annotation.PrototypeModule;
import com.github.kboyle.oktane.core.execution.DefaultCommandService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;

import java.lang.annotation.*;

@SpringBootApplication
@ComponentScan(includeFilters = @ComponentScan.Filter(PrototypeModule.class), scopeResolver = Jsr330ScopeMetadataResolver.class)
@Import(DefaultCommandService.class)
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OktaneApplication {
}
