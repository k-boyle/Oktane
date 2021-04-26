package kboyle.oktane.core.module;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.precondition.PreconditionFailedResult;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.precondition.PreconditionSuccessfulResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface Precondition {
    Mono<PreconditionResult> run(CommandContext context, Command command);

    default PreconditionResult success() {
        return PreconditionSuccessfulResult.get();
    }

    default PreconditionResult failure(String reason, Object... args) {
        return new PreconditionFailedResult(String.format(reason, args));
    }
}
