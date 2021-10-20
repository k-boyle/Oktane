package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.parsing.TypeParser;
import com.github.kboyle.oktane.core.precondition.ParameterPrecondition;
import com.github.kboyle.oktane.core.result.precondition.*;
import com.google.common.base.Preconditions;
import lombok.experimental.Delegate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.*;

public interface CommandParameter<T> extends CommandComponent {
    List<ParameterPrecondition<T>> preconditions();
    Optional<String> defaultString();
    Optional<Parameter> originalParameter();
    Class<T> type();
    TypeParser<T> typeParser();
    Command command();
    boolean remainder();
    boolean optional();
    boolean greedy();

    static <T> Builder<T> builder() {
        return new DefaultCommandParameter.Builder<>();
    }

    default ParameterPreconditionResult<T> runPreconditions(CommandContext context, T value) {
        Preconditions.checkNotNull(context, "context cannot be null");
        var preconditions = Preconditions.checkNotNull(preconditions(), "preconditions cannot be null");

        var failures = new ArrayList<ParameterPreconditionResult<T>>();
        for (var precondition : preconditions) {
            Preconditions.checkNotNull(precondition, "precondition cannot be null");

            var result = precondition.run(context, this, value);
            if (!result.success()) {
                failures.add(result);
            }
        }

        return failures.isEmpty()
            ? ParameterPreconditionSuccessfulResult.get()
            : new ParameterPreconditionsFailResult<>(failures);
    }

    interface Builder<T> {
        Builder<T> name(String name);
        Builder<T> description(String description);
        Builder<T> precondition(ParameterPrecondition<T> precondition);
        Builder<T> defaultString(String defaultString);
        Builder<T> originalParameter(Parameter originalParameter);
        Builder<T> type(Class<T> type);
        Builder<T> typeParser(TypeParser<T> typeParser);
        Builder<T> command(Command command);
        Builder<T> remainder(boolean remainder);
        Builder<T> optional(boolean optional);
        Builder<T> annotation(Annotation annotation);
        Builder<T> greedy(boolean greedy);

        CommandParameter<T> build(Command command);

        // todo method ordering
        String name();
        String description();
        List<ParameterPrecondition<T>> preconditions();
        String defaultString();
        Parameter originalParameter();
        Class<T> type();
        TypeParser<T> typeParser();
        Command command();
        boolean remainder();
        boolean optional();
        List<Annotation> annotations();
        boolean greedy();

        abstract class Delegating<T> implements Builder<T> {
            @Delegate
            protected final Builder<T> delegate;

            protected Delegating(Builder<T> delegate) {
                this.delegate = delegate;
            }
        }
    }
}
