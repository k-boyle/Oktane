package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.mapping.CommandMatch;
import com.github.kboyle.oktane.core.result.tokeniser.*;

import java.util.*;

public class DefaultTokeniser implements Tokeniser {
    @Override
    public TokeniserResult tokenise(String input, CommandMatch commandMatch) {
        var commandEnd = commandMatch.commandEnd();
        var inputLength = input.length();
        var command = commandMatch.command();
        var parameters = command.parameters();

        if (commandEnd == inputLength - 1) {
            return endOfInput(input, parameters, 0);
        }

        var currentIndex = nextCharacterIndex(input, commandEnd + 1);

        if (currentIndex == inputLength - 1) {
            return oneToken(input, currentIndex, command);
        }

        return parameterLoop(input, currentIndex, parameters);
    }

    private TokeniserResult parameterLoop(String input, int currentIndex, List<CommandParameter<?>> parameters) {
        var parameterCount = parameters.size();
        var tokens = new ArrayList<String>(parameterCount);
        var currentTokenBuilder = new StringBuilder();

        for (int p = 0; p < parameterCount; p++) {
            currentIndex = nextCharacterIndex(input, currentIndex);

            if (currentIndex == input.length()) {
                return endOfInput(input, parameters, p);
            }

            if (currentIndex == input.length() - 1 && p != parameterCount - 1) {
                var allParametersOptional = allParametersOptional(parameters, p);
                if (!allParametersOptional) {
                    return new TokeniserTooFewTokensResult(input, parameterCount);
                }

                tokens.add(Character.toString(currentIndex));
                fillNulls(tokens, parameterCount - p + 1);
                break;
            }

            var parameter = parameters.get(p);
            if (parameter.remainder()) {
                tokens.add(input.substring(currentIndex));
                return new TokeniserSuccessfulResult(tokens);
            }

            do {
                var currentCharacter = input.charAt(currentIndex);
                var startIndex = currentIndex;
                for (; currentIndex < input.length(); currentIndex++, currentCharacter = getNextChar(input, currentIndex)) {
                    if (currentCharacter == '"') {
                        if (currentIndex > 0 && input.charAt(currentIndex - 1) == '\\') {
                            if (currentIndex == input.length() - 1) {
                                currentTokenBuilder.append(input, startIndex, currentIndex - 1).append('"');
                                if (parameter.varargs()) {
                                    currentIndex++;
                                }

                                break;
                            }

                            continue;
                        }
                        var nextQuoteIndex = nextQuoteIndex(input, currentIndex + 1);
                        if (nextQuoteIndex == -1) {
                            return new TokeniserMissingQuoteResult(input, currentIndex);
                        }
                        currentTokenBuilder.append(input, startIndex + 1, nextQuoteIndex);
                        currentIndex = nextQuoteIndex + 1;
                        break;
                    } else if (currentCharacter == ' ') {
                        currentTokenBuilder.append(input, startIndex, currentIndex);
                        if (parameter.varargs()) {
                            currentIndex++;
                        }

                        break;
                    } else {
                        if (currentIndex == input.length() - 1) {
                            currentTokenBuilder.append(input, startIndex, currentIndex + 1);
                        }
                    }
                }

                tokens.add(currentTokenBuilder.toString());
                currentTokenBuilder.delete(0, currentTokenBuilder.length());
            } while (continueParsing(parameter, input, currentIndex));
        }

        if (currentIndex != input.length() - 1 && noneWhitespaceRemains(input, currentIndex)) {
            return new TokeniserTooManyTokensResult(input, parameterCount);
        }

        return new TokeniserSuccessfulResult(tokens);
    }

    private TokeniserResult endOfInput(String input, List<CommandParameter<?>> parameters, int from) {
        var parameterCount = parameters.size();
        if (parameterCount == 0) {
            return TokeniserSuccessfulResult.empty();
        }

        var allParametersOptional = allParametersOptional(parameters, from);
        if (allParametersOptional) {
            return nullTokens(parameterCount);
        }

        return new TokeniserTooFewTokensResult(input, parameterCount);
    }

    private TokeniserResult oneToken(String input, int currentIndex, Command command) {
        var currentChar = input.charAt(currentIndex);
        var parameters = command.parameters();
        var parameterCount = parameters.size();

        if (currentChar == '"') {
            return new TokeniserMissingQuoteResult(input, currentIndex);
        }

        return switch (parameterCount) {
            case 0 -> oneTokenNoParameters(input, command);
            case 1 -> new TokeniserSuccessfulResult(List.of(Character.toString(currentChar)));
            default -> oneTokenMoreThanOneParameter(input, currentChar, parameters);
        };
    }

    private TokeniserResult oneTokenNoParameters(String input, Command command) {
        if (command.ignoreExtraArguments()) {
            return nullTokens(0);
        }

        return new TokeniserTooManyTokensResult(input, command.parameters().size());
    }

    private TokeniserResult oneTokenMoreThanOneParameter(String input, char currentChar, List<CommandParameter<?>> parameters) {
        var parameterCount = parameters.size();
        var allParametersOptional = allParametersOptional(parameters, 1);
        if (!allParametersOptional) {
            return new TokeniserTooManyTokensResult(input, parameterCount);
        }

        var tokens = new ArrayList<String>();
        tokens.add(Character.toString(currentChar));
        fillNulls(tokens, parameterCount - 1);
        return new TokeniserSuccessfulResult(tokens);
    }

    private boolean allParametersOptional(List<CommandParameter<?>> parameters, int from) {
        for (int i = from, parametersSize = parameters.size(); i < parametersSize; i++) {
            var parameter = parameters.get(i);
            if (!parameter.optional()) {
                return false;
            }
        }

        return true;
    }

    private TokeniserSuccessfulResult nullTokens(int count) {
        return new TokeniserSuccessfulResult(Collections.nCopies(count, null));
    }

    private void fillNulls(List<String> tokens, int count) {
        for (int i = 0; i < count; i++) {
            tokens.add(null);
        }
    }

    private int nextCharacterIndex(String input, int currentIndex) {
        for (; currentIndex < input.length(); currentIndex++) {
            if (input.charAt(currentIndex) != ' ') {
                return currentIndex;
            }
        }

        return currentIndex;
    }

    private int nextQuoteIndex(String input, int fromIndex) {
        var nextIndex = input.indexOf('"', fromIndex);
        if (nextIndex == -1) {
            return -1;
        }

        if (input.charAt(nextIndex - 1) == '\\') {
            if (nextIndex == input.length() - 1) {
                return -1;
            }

            return nextQuoteIndex(input, nextIndex + 1);
        }

        return nextIndex;
    }

    private char getNextChar(String input, int index) {
        return index == input.length()
            ? '\0'
            : input.charAt(index);
    }

    private boolean noneWhitespaceRemains(String input, int startIndex) {
        for (int i = startIndex; i < input.length(); i++) {
            if (input.charAt(i) != ' ') {
                return true;
            }
        }

        return false;
    }

    private boolean continueParsing(CommandParameter<?> parameter, String input, int currentIndex) {
        return parameter.varargs() && noneWhitespaceRemains(input, currentIndex);
    }
}
