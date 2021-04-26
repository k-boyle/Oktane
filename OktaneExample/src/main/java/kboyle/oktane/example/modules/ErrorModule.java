package kboyle.oktane.example.modules;

import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.module.annotations.Require;
import kboyle.oktane.core.module.annotations.RequireAny;
import kboyle.oktane.core.processor.OktaneModule;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.example.ExampleCommandContext;
import kboyle.oktane.example.preconditions.FailurePrecondition;
import kboyle.oktane.example.preconditions.OrTest;

@OktaneModule
public class ErrorModule extends ModuleBase<ExampleCommandContext> {
    @Aliases("error")
    public CommandResult error() {
        return exception(new RuntimeException("Handled exception"));
    }

    @Aliases("throw")
    public CommandResult throw0() {
        throw new RuntimeException("oh no!");
    }

    @Aliases("precon")
    @Require(precondition = FailurePrecondition.class, arguments = "10")
    @Require(precondition = FailurePrecondition.class, arguments = "20")
    public CommandResult precon() {
        return nop();
    }

    @Aliases("or")
    @RequireAny({
        @Require(precondition = OrTest.class, arguments = "hi"),
        @Require(precondition = OrTest.class, arguments = "bye")
    })
    public CommandResult or() {
        return nop();
    }
}
