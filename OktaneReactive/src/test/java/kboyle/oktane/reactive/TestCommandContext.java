package kboyle.oktane.reactive;

import kboyle.oktane.reactive.module.ReactiveCommand;

public class TestCommandContext extends CommandContext {
    public TestCommandContext() {
        super(BeanProvider.empty());
    }

    public TestCommandContext(ReactiveCommand command) {
        super(BeanProvider.empty());
        super.command = command;
    }
}
