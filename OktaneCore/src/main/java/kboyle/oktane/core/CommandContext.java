package kboyle.oktane.core;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandParameter;
import kboyle.oktane.core.prefix.Prefix;

import java.util.Optional;

/**
 * Represents a POJO that will be used for passing state in commands.
 */
public class CommandContext {
    private final BeanProvider beanProvider;

    Command command;
    String input;
    Prefix prefix;
    Object[] parsedArguments;
    Object currentArgument;
    CommandParameter currentParameter;

    public CommandContext(BeanProvider beanProvider) {
        this.beanProvider = beanProvider;
    }

    public CommandContext() {
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

    /**
     * @return The input used to trigger the current execution.
     */
    public String input() {
        return input;
    }

    /**
     * @return The prefix used for the current execution.
     */
    public Optional<Prefix> prefix() {
        return Optional.ofNullable(prefix);
    }

    public Object currentArgument() {
        return currentArgument;
    }

    public CommandParameter currentParameter() {
        return currentParameter;
    }
}
