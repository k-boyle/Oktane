package kboyle.oktane.core.parsers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandParameter;
import kboyle.oktane.core.results.Result;
import kboyle.oktane.core.results.argumentparser.*;
import kboyle.oktane.core.results.typeparser.TypeParserResult;

public class GenericArgumentParser implements ArgumentParser {
    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE = '\\';
    private static final String EMPTY = "";

    private final ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass;

    public GenericArgumentParser(ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass) {
        this.typeParserByClass = typeParserByClass;
    }

    @Override
    public ArgumentParserResult parse(CommandContext context, CommandMatch commandMatch, String input) {
        Command command = commandMatch.command();
        int index = commandMatch.argumentStart();
        int commandEnd = commandMatch.commandEnd();
        ImmutableList<CommandParameter> parameters = command.parameters();

        boolean emptyParameters = parameters.isEmpty();
        if (index == commandEnd && !emptyParameters) {
            return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_FEW_ARGUMENTS, index);
        }

        if (emptyParameters) {
            if (commandEnd != index && noneWhitespaceRemains(input, index)) {
                return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_MANY_ARGUMENTS, index);
            }

            return ArgumentParserSuccessfulResult.empty();
        }

        Object[] parsedArguments = null;
        int inputLength = input.length();
        int parametersSize = parameters.size();
        for (int p = 0; p < parametersSize; p++) {
            String currentParameter = null;
            CommandParameter parameter = parameters.get(p);

            for (; index < inputLength; index++) {
                char currentCharacter = input.charAt(index);
                if (currentCharacter == SPACE) {
                    continue;
                }

                if (currentCharacter != ESCAPE || input.charAt(index - 1) == ESCAPE) {
                    break;
                }
            }

            int inputLastIndex = inputLength - 1;
            if (index == inputLastIndex && p < parametersSize - 1) {
                return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_FEW_ARGUMENTS, index);
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

                        if (index >= inputLastIndex) {
                            if (input.charAt(index - 1) != QUOTE || input.charAt(index - 2) == ESCAPE) {
                                return new ArgumentParserFailedResult(command, ParserFailedReason.MISSING_QUOTE, index);
                            }

                            currentParameter = paramStart + 1 == index
                                ? EMPTY
                                : input.substring(paramStart + 1, index);
                        }
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

            Class<?> type = parameter.type();
            if (type == String.class) {
                if (parsedArguments == null) {
                    parsedArguments = new Object[parametersSize];
                }
                parsedArguments[p] = currentParameter;
                continue;
            }

            Result parseResult = parse(parameter, context, currentParameter);
            if (parseResult instanceof TypeParserResult<?> typeParserResult) {
                if (parseResult.success()) {
                    if (parsedArguments == null) {
                        parsedArguments = new Object[parametersSize];
                    }
                    parsedArguments[p] = typeParserResult.value();
                } else {
                    return new ArgumentParserFailedToParseArgumentResult(typeParserResult);
                }
            } else {
                return (ArgumentParserExceptionResult) parseResult;
            }
        }

        if (index != inputLength && noneWhitespaceRemains(input, index)) {
            return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_MANY_ARGUMENTS, index);
        }

        return new ArgumentParserSuccessfulResult(parsedArguments);
    }

    private Result parse(CommandParameter parameter, CommandContext context, String input) {
        TypeParser<?> parser = parameter.parser();
        if (parser == null) {
            parser = Preconditions.checkNotNull(
                typeParserByClass.get(parameter.type()),
                "Missing type parser for type %s",
                parameter.type()
            );
        }

        try {
            return Preconditions.checkNotNull(parser.parse(context, input), "A type parser cannot return null");
        } catch (Exception ex) {
            return new ArgumentParserExceptionResult(context.command(), ex);
        }
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
