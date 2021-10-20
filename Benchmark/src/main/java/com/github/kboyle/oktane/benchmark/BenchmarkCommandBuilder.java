package com.github.kboyle.oktane.benchmark;

import com.github.kboyle.oktane.core.command.*;
import com.github.kboyle.oktane.core.parsing.TypeParser;
import com.github.kboyle.oktane.core.result.command.CommandNopResult;

public class BenchmarkCommandBuilder extends Command.Builder.Delegating {
    private static int counter = 0;

    public BenchmarkCommandBuilder() {
        super(Command.builder());
    }

    public <T> BenchmarkCommandBuilder parameter(Class<T> type, boolean remainder) {
        var parameter = CommandParameter.<T>builder()
            .name(String.valueOf(counter++))
            .type(type)
            .remainder(remainder)
            .typeParser(parser(type));

        parameter(parameter);
        return this;
    }

    public <T> BenchmarkCommandBuilder greedyParameter(Class<T> type) {
        var parameter = CommandParameter.<T>builder()
            .name(String.valueOf(counter++))
            .type(type)
            .greedy(true)
            .typeParser(parser(type));

        parameter(parameter);
        return this;
    }

    public Command build() {
        var dummyModule = CommandModule.builder()
            .name(String.valueOf(counter++))
            .build(null);

        callback(ctx -> new CommandNopResult(ctx.command()));

        return super.build(dummyModule);
    }


    @SuppressWarnings("unchecked")
    private <T> TypeParser<T> parser(Class<T> cl) {
        return TypeParser.simple(
            cl,
            str -> {
                if (cl == String.class) {
                    return (T) str;
                } else if (cl == int.class || cl == Integer.class) {
                    return (T) (Integer) Integer.parseInt(str);
                }

                throw new IllegalStateException();
            }
        );
    }
}
