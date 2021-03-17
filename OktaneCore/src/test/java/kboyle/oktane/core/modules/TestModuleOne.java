package kboyle.oktane.core.modules;

import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.module.TestCommandContext;
import kboyle.oktane.core.results.command.CommandResult;

import java.lang.reflect.Method;

public class TestModuleOne extends CommandModuleBase<TestCommandContext> {
    public CommandResult a() {
        return nop();
    }

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
