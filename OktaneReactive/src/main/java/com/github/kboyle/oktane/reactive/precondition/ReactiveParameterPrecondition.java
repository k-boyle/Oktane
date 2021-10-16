package com.github.kboyle.oktane.reactive.precondition;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.precondition.ParameterPrecondition;
import com.github.kboyle.oktane.core.result.precondition.*;
import reactor.core.publisher.Mono;

public interface ReactiveParameterPrecondition<T> extends ParameterPrecondition<T> {
    Mono<ParameterPreconditionResult<T>> runReactive(CommandContext context, CommandParameter<T> parameter, T value);

    @Override
    default ParameterPreconditionResult<T> run(CommandContext context, CommandParameter<T> parameter, T value) {
        throw new UnsupportedOperationException();
    }

    default Mono<ParameterPreconditionSuccessfulResult<Object>> successMono() {
        return Mono.just(ParameterPreconditionSuccessfulResult.get());
    }

    default Mono<ParameterPreconditionFailResult<T>> failureMono(CommandParameter<T> parameter, T value, String reason, Object... args) {
        return Mono.just(new ParameterPreconditionFailResult<>(parameter, value, String.format(reason, args)));
    }

    // todo or and any
}
