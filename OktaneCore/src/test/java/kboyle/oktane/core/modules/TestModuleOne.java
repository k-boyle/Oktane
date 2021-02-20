package kboyle.oktane.core.modules;

import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.module.TestCommandContext;
import kboyle.oktane.core.module.annotations.CommandDescription;
import kboyle.oktane.core.results.command.CommandResult;

import java.lang.reflect.Method;

public class TestModuleOne extends CommandModuleBase<TestCommandContext> {
    @CommandDescription(aliases = "a")
    public CommandResult a() {
        return nop();
    }

    @CommandDescription(aliases = "b")
    public CommandResult b() {
        return message("hi");
    }

    public static Method get(String method) {
        try {
            return TestModuleOne.class.getMethod(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
