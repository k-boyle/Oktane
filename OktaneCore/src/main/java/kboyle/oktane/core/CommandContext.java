package kboyle.oktane.core;

import kboyle.oktane.core.module.Command;

/**
 * Represents a pojo that will be used for passing state in commands.
 */
public abstract class CommandContext {
    private final BeanProvider beanProvider;

    Command command;

    protected CommandContext(BeanProvider beanProvider) {
        this.beanProvider = beanProvider;
    }

    protected CommandContext() {
        this(BeanProvider.empty());
    }

    /**
     * @return Gets the {@code BeanProvider} passed in.
     */
    public BeanProvider beanProvider() {
        return beanProvider;
    }

    /**
     * @return Gets current {@code Command} being executed, this will be {@code null} outside of a command method.
     */
    public Command command() {
        return command;
    }
}
