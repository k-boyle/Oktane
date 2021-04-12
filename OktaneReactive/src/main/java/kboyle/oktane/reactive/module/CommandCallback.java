package kboyle.oktane.reactive.module;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface CommandCallback {
     Mono<CommandResult> execute(CommandContext context, Object[] beans, Object[] parameters);
}
