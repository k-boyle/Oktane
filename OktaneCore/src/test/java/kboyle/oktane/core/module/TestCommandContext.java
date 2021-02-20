package kboyle.oktane.core.module;

import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;

public class TestCommandContext extends CommandContext {
    protected TestCommandContext() {
        super(BeanProvider.get());
    }
}
