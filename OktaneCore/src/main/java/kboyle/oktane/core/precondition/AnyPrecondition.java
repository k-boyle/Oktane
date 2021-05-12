package kboyle.oktane.core.precondition;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.results.Result;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.precondition.PreconditionsFailedResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class AnyPrecondition implements Precondition {
    private final ImmutableList<Precondition> preconditions;

    public AnyPrecondition(ImmutableList<Precondition> preconditions) {
        this.preconditions = preconditions;
    }

    @Override
    public Mono<PreconditionResult> run(CommandContext context, Command command) {
        return Flux.fromIterable(preconditions)
            .flatMap(precondition -> precondition.run(context, command))
            .collectList()
            .map(results -> {
                if (results.stream().anyMatch(Result::success)) {
                    return success();
                }

                return new PreconditionsFailedResult(results);
            });
    }
}
