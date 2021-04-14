package kboyle.oktane.reactive;

import kboyle.oktane.reactive.module.ReactiveCommand;

public abstract class CommandContext {
    private final BeanProvider beanProvider;

    ReactiveCommand command;

    protected CommandContext(BeanProvider beanProvider) {
        this.beanProvider = beanProvider;
    }

    protected CommandContext() {
        this(BeanProvider.empty());
    }

    public BeanProvider beanProvider() {
        return beanProvider;
    }

    public ReactiveCommand command() {
        return command;
    }
}
