package com.github.kboyle.oktane.core;

import com.github.kboyle.oktane.core.command.CommandModulesFactory;
import com.github.kboyle.oktane.core.command.TestCommandModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class OktaneTestConfiguration {
    @Bean
    public CommandModulesFactory commandModulesFactory() {
        return CommandModulesFactory.classes(TestCommandModule.class);
    }
}
