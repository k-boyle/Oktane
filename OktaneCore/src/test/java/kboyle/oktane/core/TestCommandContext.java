package kboyle.oktane.core;

import kboyle.oktane.core.module.Command;

public class TestCommandContext extends CommandContext {
    public TestCommandContext() {
        super(BeanProvider.empty());
    }

    public TestCommandContext(Command command) {
        super(BeanProvider.empty());
        super.command = command;
    }
}
