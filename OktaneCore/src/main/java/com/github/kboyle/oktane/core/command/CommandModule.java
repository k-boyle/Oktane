package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.execution.ModuleBase;
import com.github.kboyle.oktane.core.precondition.Precondition;
import com.github.kboyle.oktane.core.result.precondition.PreconditionResult;
import com.github.kboyle.oktane.core.result.precondition.PreconditionSuccessfulResult;
import lombok.experimental.Delegate;

import java.lang.annotation.Annotation;
import java.util.*;

public interface CommandModule extends CommandComponent {
    List<Precondition> preconditions();
    List<Command> commands();
    List<Class<?>> dependencies();
    List<CommandModule> children();
    Set<String> groups();
    Optional<CommandModule> parent();
    Optional<Class<? extends ModuleBase<?>>> originalClass();
    boolean singleton();
    boolean synchronised();
    Runnable before();
    Runnable after();

    static Builder builder() {
        return new DefaultCommandModule.Builder();
    }

    default PreconditionResult runPreconditions(CommandContext context, Command command) {
        var parentResult = parent()
            .map(parent -> parent.runPreconditions(context, command))
            .orElse(PreconditionSuccessfulResult.get());

        if (!parentResult.success()) {
            return parentResult;
        }

        return CommandUtil.runPreconditions(preconditions(), context, command);
    }

    interface Builder {
        Builder name(String name);
        Builder description(String description);
        Builder precondition(Precondition precondition);
        Builder command(Command.Builder command);
        Builder dependency(Class<?> dependency);
        Builder child(Builder child);
        Builder group(String group);
        Builder parent(CommandModule parent);
        Builder originalClass(Class<? extends ModuleBase<?>> originalClass);
        Builder singleton(boolean singleton);
        Builder synchronised(boolean synchronised);
        Builder annotation(Annotation annotation);
        Builder before(Runnable before);
        Builder after(Runnable after);

        String name();
        String description();
        List<Precondition> preconditions();
        List<Command.Builder> commands();
        List<Class<?>> dependencies();
        List<Builder> children();
        Set<String> groups();
        CommandModule parent();
        Class<? extends ModuleBase<?>> originalClass();
        boolean singleton();
        boolean synchronised();
        List<Annotation> annotations();
        Runnable before();
        Runnable after();

        CommandModule build(CommandModule parent);

        abstract class Delegating implements Builder {
            @Delegate
            protected final Builder delegate;

            protected Delegating(Builder delegate) {
                this.delegate = delegate;
            }
        }
    }
}
