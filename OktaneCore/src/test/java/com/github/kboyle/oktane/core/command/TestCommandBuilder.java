package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.parsing.TypeParser;
import com.github.kboyle.oktane.core.result.command.CommandNopResult;

public class TestCommandBuilder extends Command.Builder.Delegating {
    private static int moduleCounter;
    private static int commandCounter;
    private static int parameterCounter;

    public TestCommandBuilder() {
        super(Command.builder()
            .name("Command-" + commandCounter++)
            .callback(ctx -> new CommandNopResult(ctx.command())));
    }

    public <T> TestCommandBuilder parameter(Class<T> type, boolean remainder) {
        var parameter = CommandParameter.<T>builder()
            .name("Parameter-" + parameterCounter++)
            .type(type)
            .typeParser(parser(type))
            .remainder(remainder);
        delegate.parameter(parameter);
        return this;
    }

    public <T> TestCommandBuilder parameter(TypeParser<T> typeParser) {
        var parameter = CommandParameter.<T>builder()
            .name("Parameter-" + parameterCounter++)
            .type(typeParser.targetType())
            .typeParser(typeParser);
        delegate.parameter(parameter);
        return this;
    }

    public <T> TestCommandBuilder optionalParameter(Class<T> type, boolean remainder) {
        var parameter = CommandParameter.<T>builder()
            .name("Parameter-" + parameterCounter++)
            .typeParser(parser(type))
            .remainder(remainder)
            .optional(true)
            .type(type);

        delegate.parameter(parameter);
        return this;
    }

    public <T> TestCommandBuilder optionalParameter(Class<T> type, String defaultString) {
        return optionalParameter(type, defaultString, false);
    }

    public <T> TestCommandBuilder optionalParameter(Class<T> type, String defaultString, boolean remainder) {
        var parameter = CommandParameter.<T>builder()
            .name("Parameter-" + parameterCounter++)
            .typeParser(parser(type))
            .remainder(remainder)
            .optional(true)
            .defaultString(defaultString)
            .type(type);

        delegate.parameter(parameter);
        return this;
    }

    public <T> TestCommandBuilder varargs(Class<T> type) {
        return varargs(type, false);
    }

    public <T> TestCommandBuilder varargs(Class<T> type, TypeParser<T> typeParser) {
        var parameter = CommandParameter.<T>builder()
            .name("Parameter-" + parameterCounter++)
            .typeParser(typeParser)
            .varargs(true)
            .type(type);

        delegate.parameter(parameter);
        return this;
    }

    public <T> TestCommandBuilder varargs(Class<T> type, boolean remainder) {
        return varargs(type, remainder, false);
    }

    public <T> TestCommandBuilder varargs(Class<T> type, boolean remainder, boolean optional) {
        var parameter = CommandParameter.<T>builder()
            .name("Parameter-" + parameterCounter++)
            .typeParser(parser(type))
            .varargs(true)
            .remainder(remainder)
            .optional(optional)
            .type(type);

        delegate.parameter(parameter);
        return this;
    }

    public Command build() {
        var dummyModule = CommandModule.builder()
            .name("Module-" + moduleCounter++)
            .build(null);

        return build(dummyModule);
    }

    @SuppressWarnings("unchecked")
    private <T> TypeParser<T> parser(Class<T> cl) {
        return TypeParser.simple(cl, str -> {
            if (cl == String.class) {
                return (T) str;
            } else if (cl == int.class || cl == Integer.class) {
                return (T) (Integer) Integer.parseInt(str);
            }

            throw new IllegalStateException();
        });
    }
}
