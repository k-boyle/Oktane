package kboyle.oktane.reactive.module;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.results.command.CommandExceptionResult;
import kboyle.oktane.reactive.results.command.CommandMessageResult;
import kboyle.oktane.reactive.results.command.CommandNOPResult;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public abstract class ReactiveModuleBase<T extends CommandContext> {
    private T context;

    protected T context() {
        return context;
    }

    // todo figure out how to not expose this
    public void setContext(T context) {
        this.context = context;
    }

    protected Mono<CommandResult> monoMessage(String reply) {
        return Mono.just(new CommandMessageResult(context.command(), reply));
    }

    protected Mono<CommandResult> monoNop() {
        return Mono.just(new CommandNOPResult(context.command()));
    }

    protected Mono<CommandResult> monoException(Exception ex) {
        return Mono.just(new CommandExceptionResult(context.command(), ex));
    }

    protected CommandResult message(String reply) {
        return new CommandMessageResult(context.command(), reply);
    }

    protected CommandResult nop() {
        return new CommandNOPResult(context.command());
    }

    protected CommandResult exception(Exception ex) {
        return new CommandExceptionResult(context.command(), ex);
    }
}
