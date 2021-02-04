package kboyle.octane.core;

import kboyle.octane.core.module.Command;

public class TestCommandContext extends CommandContext {
    public TestCommandContext() {
        super(BeanProvider.get());
    }

    public TestCommandContext(Command command) {
        super(BeanProvider.get());
        super.command = command;
    }
}
