package com.github.kboyle.oktane.test.modules;

import com.github.kboyle.oktane.core.annotation.Aliases;
import com.github.kboyle.oktane.core.annotation.Remainder;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.execution.ModuleBase;
import com.github.kboyle.oktane.core.result.command.CommandResult;

public class TestModule extends ModuleBase<CommandContext> {
    @Aliases("ping")
    public CommandResult ping() {
        return nop();
    }

    @Aliases("remainder")
    public CommandResult remainder(@Remainder String input) {
        return text(input);
    }

    @Aliases("add")
    public CommandResult add(Integer a, int b) {
        return text(String.valueOf(a + b));
    }

    @Aliases("vargs")
    public CommandResult vargs(String... vargs) {
        return text(String.join(", ", vargs));
    }

    @Override
    protected void before() {
        System.out.println("before");
    }

    @Override
    protected void after() {
        System.out.println("after");
    }
}
