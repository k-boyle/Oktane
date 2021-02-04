package kboyle.octane.core.module;

import kboyle.octane.core.CommandContext;
import kboyle.octane.core.results.command.CommandMessageResult;
import kboyle.octane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

public abstract class CommandModuleBase<T extends CommandContext> {
    private T context;

    protected T context() {
        return context;
    }

    // todo figure out how to not expose this
    public void setContext(T context) {
        this.context = context;
    }

    protected Mono<CommandResult> message(String reply) {
        return Mono.just(CommandMessageResult.from(context.command(), reply));
    }

    protected Mono<CommandResult> empty() {
        return Mono.empty();
    }
}
