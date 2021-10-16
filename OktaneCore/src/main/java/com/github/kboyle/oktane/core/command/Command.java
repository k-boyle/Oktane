package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.execution.CommandCallback;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.precondition.Precondition;
import com.github.kboyle.oktane.core.result.precondition.PreconditionResult;
import lombok.experimental.Delegate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public interface Command extends CommandComponent, Comparable<Command> {
    List<Precondition> preconditions();
    List<CommandParameter<?>> parameters();
    Set<String> aliases();
    CommandModule module();
    CommandCallback callback();
    CommandSignature signature();
    Optional<Method> originalMethod();
    boolean synchronised();
    int priority();

    static Builder builder() {
        return new DefaultCommand.Builder();
    }

    @Override
    default int compareTo(Command other) {
        return Integer.compare(priority(), other.priority());
    }

    default PreconditionResult runPreconditions(CommandContext context) {
        var moduleResult = module().runPreconditions(context, this);

        if (!moduleResult.success()) {
            return moduleResult;
        }

        return CommandUtil.runPreconditions(preconditions(), context, this);
    }

    // todo this
    default boolean ignoreExtraArguments() {
        return false;
    }

    interface Builder {
        Builder name(String name);
        Builder description(String description);
        Builder precondition(Precondition precondition);
        Builder callback(CommandCallback callback);
        Builder parameter(CommandParameter.Builder<?> parameter);
        Builder alias(String alias);
        Builder module(CommandModule module);
        Builder originalMethod(Method originalMethod);
        Builder synchronised(boolean synchronised);
        Builder priority(int priority);
        Builder annotation(Annotation annotation);

        Command build(CommandModule module);

        default Builder maxPriority() {
            return priority(Integer.MAX_VALUE);
        }

        default Builder minPriority() {
            return priority(Integer.MIN_VALUE);
        }

        String name();
        String description();
        List<Precondition> preconditions();
        CommandCallback callback();
        List<CommandParameter.Builder<?>> parameters();
        Set<String> aliases();
        CommandModule module();
        Method originalMethod();
        boolean synchronised();
        int priority();
        List<Annotation> annotations();

        abstract class Delegating implements Builder {
            @Delegate
            protected final Builder delegate;

            protected Delegating(Builder delegate) {
                this.delegate = delegate;
            }
        }
    }
}
