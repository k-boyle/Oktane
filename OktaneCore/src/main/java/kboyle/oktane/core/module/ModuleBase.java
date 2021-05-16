package kboyle.oktane.core.module;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.command.CommandExceptionResult;
import kboyle.oktane.core.results.command.CommandMessageResult;
import kboyle.oktane.core.results.command.CommandNOPResult;
import kboyle.oktane.core.results.command.CommandResult;

/**
 * Represents a base class to designate a class as a {@link CommandModule}.
 *
 * @param <CONTEXT> The type of {@link CommandContext} to use.
 */
public abstract class ModuleBase<CONTEXT extends CommandContext> {
    private CONTEXT context;

    /**
     * @return The current execution {@link CommandContext}.
     */
    protected CONTEXT context() {
        return context;
    }

    void setContext(CONTEXT context) {
        this.context = context;
    }

    /**
     * @param message The message to return.
     * @return A {@link CommandResult} representing a simple message.
     */
    protected CommandResult message(String message) {
        return new CommandMessageResult(context.command(), message);
    }

    /**
     * @return A {@link CommandResult} representing that no-operation has happened.
     */
    protected CommandResult nop() {
        return new CommandNOPResult(context.command());
    }

    /**
     * @param ex An {@link Exception} that has occurred.
     * @return A {@link CommandResult} representing that an exception occurred.
     */
    protected CommandResult exception(Exception ex) {
        return new CommandExceptionResult(context.command(), ex);
    }
}
