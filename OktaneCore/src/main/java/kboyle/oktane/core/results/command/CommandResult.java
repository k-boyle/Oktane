package kboyle.oktane.core.results.command;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.Result;
import reactor.core.publisher.Mono;

public interface CommandResult extends Result {
    Command command();

    default Mono<CommandResult> mono() {
        return Mono.just(this);
    }
}
