package com.github.kboyle.oktane.reactive.parsing;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.parsing.TypeParser;
import com.github.kboyle.oktane.core.result.typeparser.*;
import com.github.kboyle.oktane.reactive.command.ReactiveCommandParameter;
import reactor.core.publisher.Mono;

public interface ReactiveTypeParser<T> extends TypeParser<T> {
    Mono<TypeParserResult<T>> parseReactive(CommandContext context, ReactiveCommandParameter<T> parameter, String input);

    @Override
    default TypeParserResult<T> parse(CommandContext context, CommandParameter<T> parameter, String input) {
        throw new UnsupportedOperationException();
    }

    default Mono<TypeParserSuccessfulResult<T>> successMono(T value) {
        return Mono.just(new TypeParserSuccessfulResult<>(value, this));
    }

    default Mono<TypeParserFailResult<T>> failureMono(String reason, Object... args) {
        return Mono.just(new TypeParserFailResult<>(this, String.format(reason, args)));
    }

    // todo fallback then defaults
}
