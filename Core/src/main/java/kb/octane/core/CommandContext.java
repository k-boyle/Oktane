package kb.octane.core;

import kb.octane.core.module.Command;

public abstract class CommandContext {
    private final BeanProvider beanProvider;

    Command command;

    public CommandContext(BeanProvider beanProvider) {
        this.beanProvider = beanProvider;
    }

    public BeanProvider beanProvider() {
        return beanProvider;
    }

    public Command command() {
        return command;
    }
}