package kboyle.oktane.core.parsers;

import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.results.tokeniser.TokeniserMissingQuoteResult;
import kboyle.oktane.core.results.tokeniser.TokeniserResult;
import kboyle.oktane.core.results.tokeniser.TokeniserSuccessfulResult;
import kboyle.oktane.core.results.tokeniser.TokeniserTooFewTokensResult;
import kboyle.oktane.core.results.tokeniser.TokeniserTooManyTokensResult;

import java.util.ArrayList;
import java.util.List;

public class DefaultTokeniser implements Tokeniser {
    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE = '\\';
    private static final String EMPTY = "";

    @Override
    public TokeniserResult tokenise(String input, CommandMatch commandMatch) {
        var command = commandMatch.command();
        var index = commandMatch.argumentStart();
        var commandEnd = commandMatch.commandEnd();
        var optionalStart = command.optionalStart;

        var inputLength = input.length();
        var inputLastIndex = inputLength - 1;

        var parameters = command.parameters;
        var parametersSize = parameters.size();

        var emptyParameters = parameters.isEmpty();
        if (optionalStart == - 1 && (index == commandEnd || inputLastIndex == commandEnd) && !emptyParameters) {
            return new TokeniserTooFewTokensResult(input, parametersSize);
        }

        if (emptyParameters) {
            if (commandEnd != index && noneWhitespaceRemains(input, index)) {
                return new TokeniserTooManyTokensResult(input, parametersSize);
            }

            return TokeniserSuccessfulResult.empty();
        }

        List<String> tokens = new ArrayList<>(command.parameters.size());
        for (var p = 0; p < parametersSize; p++) {
            String currentParameter = null;
            var parameter = parameters.get(p);

            for (; index < inputLength; index++) {
                var currentCharacter = input.charAt(index);
                if (currentCharacter == SPACE) {
                    continue;
                }

                if (currentCharacter != ESCAPE || input.charAt(index - 1) == ESCAPE) {
                    break;
                }
            }

            if (optionalStart != -1 && index >= optionalStart && index >= inputLastIndex) {
                return new TokeniserSuccessfulResult(tokens);
            }

            if (index > inputLastIndex) {
                return new TokeniserTooFewTokensResult(input, parametersSize);
            }

            if (parameter.remainder) {
                currentParameter = input.substring(index);
                index = inputLength;
            } else {
                var paramStart = index;
                for (; index < inputLength; index++) {
                    var currentCharacter = input.charAt(index);

                    if (currentCharacter == QUOTE) {
                        if (input.charAt(index - 1) == ESCAPE) {
                            if (index == inputLastIndex) {
                                currentParameter = input.substring(paramStart, ++index);
                                break;
                            }

                            continue;
                        }

                        index++;
                        var outerbreak = false;
                        for (; index < inputLength; index++) {
                            if (input.charAt(index) != QUOTE) {
                                continue;
                            }

                            if (input.charAt(index - 1) != ESCAPE) {
                                currentParameter = paramStart + 1 == index
                                    ? EMPTY
                                    : input.substring(paramStart + 1, index);

                                index++;
                                outerbreak = true;
                                break;
                            }
                        }

                        if (outerbreak) {
                            break;
                        }

                        return new TokeniserMissingQuoteResult(input, index);
                    } else if (currentCharacter == SPACE) {
                        currentParameter = input.substring(paramStart, index);
                        break;
                    } else if (index == inputLastIndex) {
                        currentParameter = paramStart == 0 ? input : input.substring(paramStart);
                        index++;
                        break;
                    }
                }
            }

            tokens.add(currentParameter);
        }

        if (index != inputLength && noneWhitespaceRemains(input, index)) {
            return new TokeniserTooManyTokensResult(input, parametersSize);
        }

        return new TokeniserSuccessfulResult(tokens);
    }

    private static boolean noneWhitespaceRemains(String input, int index) {
        for (; index < input.length(); index++) {
            if (input.charAt(index) != SPACE) {
                return true;
            }
        }

        return false;
    }
}
