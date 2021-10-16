package com.github.kboyle.oktane.reactive.precondition;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.precondition.Precondition;
import com.github.kboyle.oktane.core.result.precondition.*;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ReactivePrecondition extends Precondition {
    Mono<PreconditionResult> runReactive(CommandContext context, Command command);

    @Override
    default PreconditionResult run(CommandContext context, Command command) {
        throw new UnsupportedOperationException();
    }

    default Mono<PreconditionSuccessfulResult> successMono() {
        return Mono.just(PreconditionSuccessfulResult.get());
    }

    default Mono<PreconditionFailResult> failureMono(String reason, Object... args) {
        return Mono.just(new PreconditionFailResult(this, String.format(reason, args)));
    }

    // todo or and any
}
