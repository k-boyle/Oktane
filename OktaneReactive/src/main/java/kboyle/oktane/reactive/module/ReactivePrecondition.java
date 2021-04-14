package kboyle.oktane.reactive.module;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.results.precondition.PreconditionFailedResult;
import kboyle.oktane.reactive.results.precondition.PreconditionResult;
import kboyle.oktane.reactive.results.precondition.PreconditionSuccessfulResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ReactivePrecondition {
    Mono<PreconditionResult> SUCCESS = Mono.just(PreconditionSuccessfulResult.get());

    Mono<PreconditionResult> run(CommandContext context, ReactiveCommand command);

    default Mono<PreconditionResult> monoSuccess() {
        return SUCCESS;
    }

    default PreconditionResult success() {
        return PreconditionSuccessfulResult.get();
    }

    default Mono<PreconditionResult> monoFailure(String reason, Object... args) {
        return Mono.just(new PreconditionFailedResult(String.format(reason, args)));
    }

    default PreconditionResult failure(String reason, Object... args) {
        return new PreconditionFailedResult(String.format(reason, args));
    }
}
