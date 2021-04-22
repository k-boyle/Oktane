package kboyle.oktane.reactive.module;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.results.precondition.PreconditionFailedResult;
import kboyle.oktane.reactive.results.precondition.PreconditionResult;
import kboyle.oktane.reactive.results.precondition.PreconditionSuccessfulResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ReactivePrecondition {
    Mono<PreconditionResult> run(CommandContext context, ReactiveCommand command);

    default PreconditionResult success() {
        return PreconditionSuccessfulResult.get();
    }

    default PreconditionResult failure(String reason, Object... args) {
        return new PreconditionFailedResult(String.format(reason, args));
    }
}
