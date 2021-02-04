package kboyle.octane.core.module;

import kboyle.octane.core.CommandContext;
import kboyle.octane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface CommandCallback {
     Mono<CommandResult> execute(CommandContext context, Object[] services, Object[] parameters);
}
