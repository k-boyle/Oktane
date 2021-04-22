package kboyle.oktane.reactive.module;

import kboyle.oktane.reactive.BeanProvider;
import kboyle.oktane.reactive.CommandContext;

public class TestCommandContext extends CommandContext {
    protected TestCommandContext() {
        super(BeanProvider.empty());
    }
}
