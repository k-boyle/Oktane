package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.argumentparser.*;
import com.github.kboyle.oktane.core.result.typeparser.TypeParserResult;
import com.google.common.base.Defaults;
import com.google.common.base.Preconditions;

import java.lang.reflect.Array;
import java.util.List;

class DefaultArgumentParser implements ArgumentParser {
    // todo this desperately needs cleaning up
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ArgumentParserResult parse(CommandContext context, Command command, List<String> tokens) {
        var parameters = command.parameters();
        if (parameters.isEmpty()) {
            return ArgumentParserSuccessfulResult.empty();
        }

        var parsedArguments = new Object[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            var parameter = parameters.get(i);

            if (parameter.varargs()) {
                return parseVarargs(context, tokens, parsedArguments, i, parameter);
            }

            var token = getNextToken(tokens, i);
            if (token == null) {
                if (parameter.defaultString().isEmpty()) {
                    parsedArguments[i] = Defaults.defaultValue(parameter.type());
                    continue;
                }

                token = parameter.defaultString().get();
            }

            var result = parse(context, parameter, token);

            if (!result.success()) {
                return new ArgumentParserTypeParserFailResult(parameter, token, result);
            }

            parsedArguments[i] = result.value();
        }

        return new ArgumentParserSuccessfulResult(parsedArguments);
    }

    private ArgumentParserResult parseVarargs(
            CommandContext context,
            List<String> tokens,
            Object[] parsedArguments,
            int i,
            CommandParameter<?> parameter) {

        var remainingTokens = tokens.size() - i;
        Object vargs;

        if (remainingTokens != 0) {
            vargs = Array.newInstance(parameter.type(), remainingTokens);

            for (int j = 0; j < remainingTokens; j++) {
                var token = tokens.get(i + j);
                var result = parse(context, parameter, token);

                if (!result.success()) {
                    return new ArgumentParserTypeParserFailResult(parameter, token, result);
                }

                Array.set(vargs, j, result.value());
            }
        } else {
            vargs = Array.newInstance(parameter.type(), 1);

            if (parameter.defaultString().isEmpty()) {
                Array.set(vargs, 0, Defaults.defaultValue(parameter.type()));
            } else {
                var token = parameter.defaultString().get();
                var result = parse(context, parameter, token);

                if (!result.success()) {
                    return new ArgumentParserTypeParserFailResult(parameter, token, result);
                }

                Array.set(vargs, 0, result.value());
            }
        }

        parsedArguments[i] = vargs;
        return new ArgumentParserSuccessfulResult(parsedArguments);
    }

    private String getNextToken(List<String> tokens, int index) {
        if (index >= tokens.size()) {
            return null;
        }

        return tokens.get(index);
    }

    private <T> TypeParserResult<T> parse(CommandContext context, CommandParameter<T> parameter, String token) {
        var parser = Preconditions.checkNotNull(parameter.typeParser(), "CommandParameter#typeParser cannot return null");
        return Preconditions.checkNotNull(parser.parse(context, parameter, token), "TypeParser#parse cannot return null");
    }
}
