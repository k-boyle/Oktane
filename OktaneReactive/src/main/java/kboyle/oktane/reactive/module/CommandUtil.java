package kboyle.oktane.reactive.module;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.results.precondition.PreconditionResult;
import kboyle.oktane.reactive.results.precondition.PreconditionSuccessfulResult;
import kboyle.oktane.reactive.results.precondition.PreconditionsFailedResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class CommandUtil {
    private static final Mono<PreconditionResult> SUCCESS = Mono.just(PreconditionSuccessfulResult.get());

    private CommandUtil() {
    }

    public static Mono<PreconditionResult> runPreconditions(CommandContext context, ReactiveCommand command, ImmutableList<ReactivePrecondition> preconditions) {
        if (preconditions.isEmpty()) {
            return SUCCESS;
        }

        return Flux.fromIterable(preconditions)
            .flatMap(precondition -> precondition.run(context, command))
            .collectList()
            .map(results -> {
                ImmutableList<PreconditionResult> failedResults = results.stream()
                    .filter(result -> !result.success())
                    .collect(ImmutableList.toImmutableList());

                if (failedResults.isEmpty()) {
                    return PreconditionSuccessfulResult.get();
                }

                return new PreconditionsFailedResult(failedResults);
            });
    }
}
