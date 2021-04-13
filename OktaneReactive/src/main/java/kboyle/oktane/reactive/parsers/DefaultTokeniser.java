package kboyle.oktane.reactive.parsers;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.reactive.mapping.CommandMatch;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.ReactiveCommandParameter;
import kboyle.oktane.reactive.results.tokeniser.*;

import java.util.ArrayList;
import java.util.List;

public class DefaultTokeniser implements Tokeniser {
    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE = '\\';
    private static final String EMPTY = "";

    @Override
    public TokeniserResult tokenise(String input, CommandMatch commandMatch) {
        ReactiveCommand command = commandMatch.command();
        int index = commandMatch.argumentStart();
        int commandEnd = commandMatch.commandEnd();

        int inputLength = input.length();
        int inputLastIndex = inputLength - 1;

        ImmutableList<ReactiveCommandParameter> parameters = command.parameters();
        int parametersSize = parameters.size();

        boolean emptyParameters = parameters.isEmpty();
        if ((index == commandEnd || inputLastIndex == commandEnd) && !emptyParameters) {
            return new TokeniserTooFewTokensResult(input, parametersSize);
        }

        if (emptyParameters) {
            if (commandEnd != index && noneWhitespaceRemains(input, index)) {
                return new TokeniserTooManyTokensResult(input, parametersSize);
            }

            return TokeniserSuccessfulResult.empty();
        }

        List<String> tokens = new ArrayList<>(command.parameters().size());
        for (int p = 0; p < parametersSize; p++) {
            String currentParameter = null;
            ReactiveCommandParameter parameter = parameters.get(p);

            for (; index < inputLength; index++) {
                char currentCharacter = input.charAt(index);
                if (currentCharacter == SPACE) {
                    continue;
                }

                if (currentCharacter != ESCAPE || input.charAt(index - 1) == ESCAPE) {
                    break;
                }
            }

            if (index > inputLastIndex) {
                return new TokeniserTooFewTokensResult(input, parametersSize);
            }

            if (parameter.remainder()) {
                currentParameter = input.substring(index);
                index = inputLength;
            } else {
                int paramStart = index;
                for (; index < inputLength; index++) {
                    char currentCharacter = input.charAt(index);

                    if (currentCharacter == QUOTE) {
                        if (input.charAt(index - 1) == ESCAPE) {
                            if (index == inputLastIndex) {
                                currentParameter = input.substring(paramStart, ++index);
                                break;
                            }

                            continue;
                        }

                        index++;
                        boolean outerbreak = false;
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
