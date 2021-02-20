package kboyle.oktane.core.modules;

import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.module.TestCommandContext;
import kboyle.oktane.core.module.annotations.CommandDescription;
import kboyle.oktane.core.results.command.CommandResult;

import java.lang.reflect.Method;

public class TestModuleTwo extends CommandModuleBase<TestCommandContext> {
    private final int a;

    public TestModuleTwo(int a) {
        this.a = a;
    }

    @CommandDescription(aliases = "a")
    public CommandResult a(String str) {
        return message(str);
    }

    public static Method get(String method) {
        try {
            return TestModuleOne.class.getMethod(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
