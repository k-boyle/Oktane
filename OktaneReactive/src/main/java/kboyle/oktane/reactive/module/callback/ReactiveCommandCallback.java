package kboyle.oktane.reactive.module.callback;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ReactiveCommandCallback {
     Mono<CommandResult> execute(CommandContext context, Object[] beans, Object[] parameters);
}
