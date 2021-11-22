package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.prefix.Prefix;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;

public class CommandContext {
    private final ApplicationContext applicationContext;

    Command command;
    Prefix<?> prefix;
    List<String> tokens;
    Object[] arguments;
    Object[] dependencies;

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
        return dependencies;
    }
}
