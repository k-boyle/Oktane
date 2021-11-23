package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.Utilities;
import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.prefix.Prefix;
import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;

public class CommandContext {
    private final ApplicationContext applicationContext;

    Command command;
    Prefix<?> prefix;
    List<String> tokens;
    Object[] arguments;

    public CommandContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public CommandContext() {
        this(null);
    }

    public ApplicationContext applicationContext() {
        return applicationContext;
    }

    public Command command() {
        return command;
    }

    public Optional<Prefix<?>> prefix() {
        return Optional.ofNullable(prefix);
    }

    public List<String> tokens() {
        return tokens;
    }

    public Object[] arguments() {
        return arguments;
    }

    public Object[] dependencies() {
        Preconditions.checkNotNull(command, "Cannot get the dependencies before command is chosen");
        Preconditions.checkNotNull(applicationContext, "An application context is required for dependency injection");

        var module = command.module();
        var dependencyClasses = module.dependencies();
        return Utilities.Spring.getBeans(applicationContext, dependencyClasses);
    }
}
