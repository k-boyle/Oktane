package kb.octane.core.module;

import kb.octane.core.CommandContext;
import kb.octane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface CommandCallback {
     Mono<CommandResult> execute(CommandContext context, Object[] services, Object[] parameters);
}
