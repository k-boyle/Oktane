package com.github.kboyle.oktane.core.configuration;

import com.github.kboyle.oktane.core.mapping.CommandMap;
import com.github.kboyle.oktane.core.mapping.CommandMapProvider;
import com.github.kboyle.oktane.core.parsing.*;
import com.github.kboyle.oktane.core.prefix.PrefixSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OktaneConfiguration {
    @Bean
    public PrefixSupplier prefixSupplier() {
        return PrefixSupplier.empty();
    }

    @Bean
    public CommandMapProvider commandMapProvider() {
        return CommandMap.provider();
    }

    @Bean
    public Tokeniser tokeniser() {
        return Tokeniser.get();
    }

    @Bean
    public ArgumentParser argumentParser() {
        return ArgumentParser.get();
    }

    @Bean
    public TypeParserProvider typeParserProvider() {
        return TypeParserProvider.defaults();
    }
}
