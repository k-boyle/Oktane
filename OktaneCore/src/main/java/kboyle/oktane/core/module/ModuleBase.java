package kboyle.oktane.core.module;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.command.CommandExceptionResult;
import kboyle.oktane.core.results.command.CommandMessageResult;
import kboyle.oktane.core.results.command.CommandNOPResult;
import kboyle.oktane.core.results.command.CommandResult;

public abstract class ModuleBase<T extends CommandContext> {
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
