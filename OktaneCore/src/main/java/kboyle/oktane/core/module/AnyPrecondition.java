package kboyle.oktane.core.module;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.CommandContext;
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
            .switchOnFirst(
                (signal, flux) -> {
                    if (signal.hasValue()) {
                        PreconditionResult result = signal.get();
                        if (result.success()) {
                            return Flux.just(result);
                        }
                    }

                    return flux;
                },
                true)
            .collectList()
            .map(results -> {
                if (results.size() == 1 && results.get(0).success()) {
                    return success();
                }

                return new PreconditionsFailedResult(results);
            });
    }
}
