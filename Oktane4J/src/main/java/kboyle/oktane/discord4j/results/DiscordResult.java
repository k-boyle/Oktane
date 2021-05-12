package kboyle.oktane.discord4j.results;

import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

public abstract class DiscordResult implements CommandResult {
    public abstract Mono<Void> execute();

    public Mono<CommandResult> mono() {
        return Mono.just(this);
    }
}
