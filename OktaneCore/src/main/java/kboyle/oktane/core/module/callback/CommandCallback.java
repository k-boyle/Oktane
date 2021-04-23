package kboyle.oktane.core.module.callback;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface CommandCallback {
     Mono<CommandResult> execute(CommandContext context, Object[] beans, Object[] parameters);
}
