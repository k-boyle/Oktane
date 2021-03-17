package kboyle.oktane.example.modules;

import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.module.annotations.Require;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.example.ExampleCommandContext;
import kboyle.oktane.example.preconditions.FailurePrecondition;

public class ErrorModule extends CommandModuleBase<ExampleCommandContext> {
    @Aliases("error")
    public CommandResult error() {
        return error(new RuntimeException("Handled exception"));
    }

    @Aliases("throw")
    public CommandResult throw0() {
        throw new RuntimeException("oh no!");
    }

    @Aliases("precon")
    @Require(precondition = FailurePrecondition.class, arguments = "10")
    @Require(precondition = FailurePrecondition.class, arguments = "10")
    public CommandResult precon() {
        return nop();
    }
}
