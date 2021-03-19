package kboyle.oktane.core.parsers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandParameter;
import kboyle.oktane.core.results.Result;
import kboyle.oktane.core.results.argumentparser.*;
import kboyle.oktane.core.results.typeparser.TypeParserResult;

public class DefaultArgumentParser implements ArgumentParser {
    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE = '\\';

    private final ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass;

    public DefaultArgumentParser(ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass) {
        this.typeParserByClass = typeParserByClass;
    }

    @Override
    public ArgumentParserResult parse(CommandContext context, String input, int index) {
        return parse(context, context.command(), input, index);
    }

    public ArgumentParserResult parse(CommandContext context, Command command, String input, int index) {
        ImmutableList<CommandParameter> parameters = command.parameters();

        if (input.length() <= index && !parameters.isEmpty()) {
            return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_FEW_ARGUMENTS, index);
        }

        if (parameters.isEmpty()) {
            if (input.length() != 0 && input.length() - 1 != index && noneWhitespaceRemains(input, index)) {
                return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_MANY_ARGUMENTS, index);
            }
            return ArgumentParserSuccessfulResult.empty();
        }

        Object[] parsedArguments = null;
        for (int p = 0; p < parameters.size(); p++) {
            String currentParameter = null;
            CommandParameter parameter = parameters.get(p);

            for (; index < input.length(); index++) {
                char currentCharacter = input.charAt(index);
                if (Character.isSpaceChar(currentCharacter)) {
                    continue;
                }

                if (currentCharacter != ESCAPE || input.charAt(index - 1) == ESCAPE) {
                    break;
                }
            }

            if (index == input.length() - 1) {
                return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_FEW_ARGUMENTS, index);
            }

            if (parameter.remainder()) {
                currentParameter = input.substring(index);
                index = input.length();
            } else {
                int paramStart = index;
                for (; index < input.length(); index++) {
                    char currentCharacter = input.charAt(index);

                    if (currentCharacter == QUOTE) {
                        if (input.charAt(index - 1) == ESCAPE) {
                            continue;
                        }

                        index++;
                        for (; index < input.length(); index++) {
                            if (input.charAt(index) != QUOTE) {
                                continue;
                            }

                            if (input.charAt(index - 1) != ESCAPE) {
                                currentParameter = paramStart + 1 == index
                                    ? ""
                                    : input.substring(paramStart + 1, index + 1);

                                break;
                            }
                        }

                        if (index >= input.length() - 1) {
                            return new ArgumentParserFailedResult(command, ParserFailedReason.MISSING_QUOTE, index);
                        }
                    } else if (currentCharacter == SPACE) {
                        currentParameter = input.substring(paramStart, index);
                        break;
                    } else if (index == input.length() - 1) {
                        currentParameter = paramStart == 0 ? input : input.substring(paramStart);
                        index++;
                        break;
                    }
                }
            }

            Class<?> type = parameter.type();
            if (type == String.class) {
                if (parsedArguments == null) {
                    parsedArguments = new Object[parameters.size()];
                }
                parsedArguments[p] = currentParameter;
                continue;
            }

            Result parseResult = parse(type, context, currentParameter);
            if (parseResult instanceof TypeParserResult<?> typeParserResult) {
                if (parseResult.success()) {
                    if (parsedArguments == null) {
                        parsedArguments = new Object[parameters.size()];
                    }
                    parsedArguments[p] = typeParserResult.value();
                } else {
                    return new ArgumentParserFailedToParseArgumentResult(typeParserResult);
                }
            } else {
                return (ArgumentParserExceptionResult) parseResult;
            }
        }

        if (index != input.length() && noneWhitespaceRemains(input, index)) {
            return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_MANY_ARGUMENTS, index);
        }

        return new ArgumentParserSuccessfulResult(parsedArguments);
    }

    private Result parse(Class<?> type, CommandContext context, String input) {
        TypeParser<?> typeParser = Preconditions.checkNotNull(
            typeParserByClass.get(type),
            "Missing type parser for type %s",
            type
        );

        try {
            return Preconditions.checkNotNull(typeParser.parse(context, input), "A type parser cannot return null");
        } catch (Exception ex) {
            return new ArgumentParserExceptionResult(context.command(), ex);
        }
    }

    private static boolean noneWhitespaceRemains(String input, int index) {
        for (; index < input.length(); index++) {
            if (!Character.isSpaceChar(input.charAt(index))) {
                return true;
            }
        }

        return false;
    }
}
