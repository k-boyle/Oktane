package kboyle.oktane.reactive.module;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.results.command.CommandExceptionResult;
import kboyle.oktane.reactive.results.command.CommandMessageResult;
import kboyle.oktane.reactive.results.command.CommandNOPResult;
import kboyle.oktane.reactive.results.command.CommandResult;

public abstract class ReactiveModuleBase<T extends CommandContext> {
    private T context;

    protected T context() {
        return context;
    }

    // todo figure out how to not expose this
    public void setContext(T context) {
        this.context = context;
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
