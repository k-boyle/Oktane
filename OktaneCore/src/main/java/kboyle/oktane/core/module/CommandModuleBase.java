package kboyle.oktane.core.module;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.command.CommandMessageResult;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.core.results.command.CommandSuccessfulResult;
import kboyle.oktane.core.results.command.ExecutionErrorResult;

public abstract class CommandModuleBase<T extends CommandContext> {
    private T context;

    protected T context() {
        return context;
    }

    // todo figure out how to not expose this
    public void setContext(T context) {
        this.context = context;
    }

    protected CommandResult message(String reply) {
        return CommandMessageResult.from(context.command(), reply);
    }

    protected CommandResult nop() {
        return new CommandSuccessfulResult.NOP(context.command());
    }

    protected CommandResult error(Exception ex) {
        return new ExecutionErrorResult(context.command(), ex);
    }
}
