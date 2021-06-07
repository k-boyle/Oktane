package kboyle.oktane.example.modules;

import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.example.ExampleCommandContext;
import kboyle.oktane.example.preconditions.RequireFailure;
import kboyle.oktane.example.preconditions.RequireHi;

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
    @RequireFailure(10)
    public CommandResult precon() {
        return nop();
    }

    @Aliases("or")
    @RequireHi(value = "hi", group = "group1")
    @RequireHi(value = "bye", group = "group1")
    @RequireHi(value = "hi", group = "group2")
    @RequireHi(value = "bye", group = "group2")
    public CommandResult or() {
        return nop();
    }
}