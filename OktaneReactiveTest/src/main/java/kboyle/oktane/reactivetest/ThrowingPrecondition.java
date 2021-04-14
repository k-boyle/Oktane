package kboyle.oktane.reactivetest;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.ReactivePrecondition;
import kboyle.oktane.reactive.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

public class ThrowingPrecondition implements ReactivePrecondition {
    @Override
    public Mono<PreconditionResult> run(CommandContext context, ReactiveCommand command) {
        throw new RuntimeException();
    }
}
