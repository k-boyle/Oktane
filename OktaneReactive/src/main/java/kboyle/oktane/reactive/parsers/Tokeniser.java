package kboyle.oktane.reactive.parsers;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.reactive.mapping.CommandMatch;
import kboyle.oktane.reactive.module.Command;
import kboyle.oktane.reactive.module.CommandParameter;
import kboyle.oktane.reactive.results.tokeniser.*;

import java.util.ArrayList;
import java.util.List;

public class Tokeniser {
    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE = '\\';

    public TokeniserResult tokenise(String input, CommandMatch commandMatch) {
        Command command = commandMatch.command();
        int index = commandMatch.argumentStart();
        int commandEnd = commandMatch.commandEnd();
        int inputLength = input.length();
        int inputLastIndex = inputLength - 1;
        ImmutableList<CommandParameter> parameters = command.parameters();
        int parameterCount = parameters.size();

        boolean emptyParameters = parameters.isEmpty();
        if ((index == commandEnd || inputLastIndex == commandEnd) && !emptyParameters) {
            return new TokeniserTooFewTokensResult(input, parameterCount);
        }

        if (emptyParameters) {
            if (commandEnd != index && noneWhitespaceRemains(input, index)) {
                return new TokeniserTooManyTokensResult(input, parameterCount);
            }

            return TokeniserSuccessfulResult.empty();
        }


        List<String> tokens = new ArrayList<>(command.parameters().size());

        // todo use StringBuilder to build up tokens (pool them maybe)
        // todo handle remainder
        // todo check parameter count stuff works
        for (; index < inputLength; index++) {
            int tokenStart = index;
            char currentCharacter = input.charAt(index);

            if (currentCharacter == QUOTE) {
                if (input.charAt(index - 1) == ESCAPE) {
                    if (index == inputLastIndex) {
                        tokens.add(input.substring(tokenStart, ++index));
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
                        tokens.add(input.substring(tokenStart + 1, index));

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
                tokens.add(input.substring(tokenStart, index));
            } else if (index == inputLastIndex) {
                tokens.add(input.substring(tokenStart));
            }
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
