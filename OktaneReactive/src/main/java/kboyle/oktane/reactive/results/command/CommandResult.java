package kboyle.oktane.reactive.results.command;

import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.Result;
import reactor.core.publisher.Mono;

public interface CommandResult extends Result {
    ReactiveCommand command();

    default Mono<CommandResult> mono() {
        return Mono.just(this);
    }
}
