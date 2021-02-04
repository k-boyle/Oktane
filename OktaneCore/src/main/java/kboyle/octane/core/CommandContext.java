package kboyle.octane.core;

import kboyle.octane.core.module.Command;

public abstract class CommandContext {
    private final BeanProvider beanProvider;

    Command command;

    protected CommandContext(BeanProvider beanProvider) {
        this.beanProvider = beanProvider;
    }

    public BeanProvider beanProvider() {
        return beanProvider;
    }

    public Command command() {
        return command;
    }
}
