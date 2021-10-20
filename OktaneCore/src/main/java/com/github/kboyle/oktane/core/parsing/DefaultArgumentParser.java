package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.argumentparser.*;
import com.github.kboyle.oktane.core.result.typeparser.TypeParserResult;
import com.google.common.base.Defaults;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class DefaultArgumentParser implements ArgumentParser {
    @Override
    public ArgumentParserResult parse(CommandContext context) {
        var parameters = context.command().parameters();
        if (parameters.isEmpty()) {
            return ArgumentParserSuccessfulResult.empty();
        }

        var parametersSize = parameters.size();
        var parsedArguments = new Object[parametersSize];
        var tokens = context.tokens();
        int tokenIndex = 0, parameterIndex = 0;
        for (int tokensSize = tokens.size(); tokenIndex < tokensSize; tokenIndex++) {
            var token = tokens.get(tokenIndex);
            var parameter = parameters.get(parameterIndex);

            if (parameter.greedy()) {
                var fullBelly = tokensSize - tokenIndex - (parametersSize - parameterIndex) + 1 == 0;
                if (fullBelly) {
                    parameterIndex++;
                    parameter = parameters.get(parameterIndex);
                }
            }

            if (token == null) {
                if (parameter.defaultString().isEmpty()) {
                    parameterIndex = setArgument(parameterIndex, parsedArguments, parameter, Defaults.defaultValue(parameter.type()), tokensSize - tokenIndex);
                    continue;
                }

                token = parameter.defaultString().get();
            }

            var result = parse(context, parameter, token);

            if (!result.success()) {
                if (!parameter.greedy() || parameterIndex == parametersSize - 1 || parsedArguments[parameterIndex] == null) {
                    return new ArgumentParserTypeParserFailResult(parameter, token, result);
                }

                tokenIndex--;
                parameterIndex++;
            }

            parameterIndex = setArgument(parameterIndex, parsedArguments, parameter, result.value(), tokensSize - tokenIndex);
        }

        return new ArgumentParserSuccessfulResult(parsedArguments);
    }

    private <T> TypeParserResult<T> parse(CommandContext context, CommandParameter<T> parameter, String token) {
        var parser = Preconditions.checkNotNull(parameter.typeParser(), "CommandParameter#typeParser cannot return null");
        return Preconditions.checkNotNull(parser.parse(context, parameter, token), "TypeParser#parse cannot return null");
    }

    private int setArgument(
            int parameterIndex,
            Object[] parsedArguments,
            CommandParameter<?> parameter,
            Object value,
            int remainingTokens) {

        if (!parameter.greedy()) {
            parsedArguments[parameterIndex] = value;
            return parameterIndex + 1;
        }

        handleGreedy(parsedArguments, parameterIndex, value);

        return remainingTokens == 1
            ? parameterIndex + 1
            : parameterIndex;
    }

    @SuppressWarnings("unchecked")
    private <T> void handleGreedy(Object[] parsedArguments, int parameterIndex, T value) {
        var greedyList = (List<T>) parsedArguments[parameterIndex];
        if (greedyList == null) {
            greedyList = new ArrayList<>();
            parsedArguments[parameterIndex] = greedyList;
        }

        greedyList.add(value);
    }
}
