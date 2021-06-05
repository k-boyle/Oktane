package kboyle.oktane.discord4j.results;

import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

public interface DiscordResult extends CommandResult {
    Mono<Void> execute();
}
