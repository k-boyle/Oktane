package kboyle.oktane.core;

import kboyle.oktane.core.module.Command;

public abstract class CommandContext {
    private final BeanProvider beanProvider;

    Command command;

    protected CommandContext(BeanProvider beanProvider) {
        this.beanProvider = beanProvider;
    }

    protected CommandContext() {
        this(BeanProvider.empty());
    }

    public BeanProvider beanProvider() {
        return beanProvider;
    }

    public Command command() {
        return command;
    }
}
