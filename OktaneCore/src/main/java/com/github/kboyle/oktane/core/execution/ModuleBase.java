package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.annotation.PrototypeModule;
import com.github.kboyle.oktane.core.result.command.*;

@PrototypeModule
public abstract class ModuleBase<CONTEXT extends CommandContext> {
    CONTEXT context;

    protected CONTEXT context() {
        return context;
    }

    protected void before() {
    }

    protected void after() {
    }

    protected CommandResult nop() {
        return new CommandNopResult(context().command);
    }

    protected CommandResult text(String text) {
        return new CommandTextResult(context().command, text);
    }

    protected CommandResult text(String text, Object... args) {
        return new CommandTextResult(context().command, String.format(text, args));
    }

    protected CommandResult exception(Exception ex) {
        return new CommandExceptionResult(context().command, ex);
    }
}
