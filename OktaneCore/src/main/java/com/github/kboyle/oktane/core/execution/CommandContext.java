package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.dependency.DependencyProvider;
import com.github.kboyle.oktane.core.prefix.Prefix;

import java.util.List;
import java.util.Optional;

public class CommandContext {
    private final DependencyProvider dependencyProvider;

    Command command;
    Prefix<?> prefix;
    List<String> tokens;
    Object[] arguments;
    Object[] dependencies;

    public CommandContext(DependencyProvider dependencyProvider) {
        this.dependencyProvider = dependencyProvider;
    }

    public CommandContext() {
        this(DependencyProvider.empty());
    }

    public DependencyProvider dependencyProvider() {
        return dependencyProvider;
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