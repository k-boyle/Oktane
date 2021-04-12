package kboyle.oktane.reactive.module;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.results.precondition.PreconditionFailedResult;
import kboyle.oktane.reactive.results.precondition.PreconditionResult;
import kboyle.oktane.reactive.results.precondition.PreconditionSuccessfulResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface Precondition {
    Mono<PreconditionSuccessfulResult> SUCCESS = Mono.just(PreconditionSuccessfulResult.get());

    Mono<PreconditionResult> run(CommandContext context, Command command);

    default Mono<PreconditionSuccessfulResult> success() {
        return SUCCESS;
    }

    default Mono<PreconditionFailedResult> failure(String reason, Object... args) {
        return Mono.just(new PreconditionFailedResult(String.format(reason, args)));
    }
}
