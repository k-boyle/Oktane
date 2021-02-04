package kboyle.octane.core.parsers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kboyle.octane.core.CommandContext;
import kboyle.octane.core.exceptions.InvalidResultException;
import kboyle.octane.core.module.Command;
import kboyle.octane.core.module.CommandParameter;
import kboyle.octane.core.results.ExecutionErrorResult;
import kboyle.octane.core.results.FailedResult;
import kboyle.octane.core.results.Result;
import kboyle.octane.core.results.argumentparser.FailedArgumentParserResult;
import kboyle.octane.core.results.argumentparser.SuccessfulArgumentParserResult;
import kboyle.octane.core.results.typeparser.SuccessfulTypeParserResult;

public class DefaultArgumentParser implements ArgumentParser {
    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE = '\\';

    private final ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass;

    public DefaultArgumentParser(ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass) {
        this.typeParserByClass = typeParserByClass;
    }

    @Override
    public Result parse(CommandContext context, String input, int index) {
        return parse(context, context.command(), input, index);
    }

    public Result parse(CommandContext context, Command command, String input, int index) {
        ImmutableList<CommandParameter> parameters = command.parameters();

        if (input.length() <= index && !parameters.isEmpty()) {
            return new FailedArgumentParserResult(command, FailedArgumentParserResult.Reason.TOO_FEW_ARGUMENTS, index);
        }

        if (parameters.isEmpty()) {
            if (input.length() == 0 || input.length() - 1 == index) {
                return SuccessfulArgumentParserResult.empty();
            } else {
                return new FailedArgumentParserResult(command, FailedArgumentParserResult.Reason.TOO_MANY_ARGUMENTS, index);
            }
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
                return new FailedArgumentParserResult(command, FailedArgumentParserResult.Reason.TOO_FEW_ARGUMENTS, index);
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
                            return new FailedArgumentParserResult(command, FailedArgumentParserResult.Reason.MISSING_QUOTE, index);
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
            if (parseResult instanceof SuccessfulTypeParserResult success) {
                if (parsedArguments == null) {
                    parsedArguments = new Object[parameters.size()];
                }
                parsedArguments[p] = success.value();
            } else if (parseResult instanceof FailedResult failedResult) {
                return failedResult;
            } else {
                throw new InvalidResultException(SuccessfulTypeParserResult.class, parseResult.getClass());
            }
        }

        if (index != input.length()) {
            return new FailedArgumentParserResult(command, FailedArgumentParserResult.Reason.TOO_MANY_ARGUMENTS, index);
        }

        return new SuccessfulArgumentParserResult(parsedArguments);
    }

    private Result parse(Class<?> type, CommandContext context, String input) {
        TypeParser<?> typeParser = Preconditions.checkNotNull(
            typeParserByClass.get(type),
            "Missing type parser for type %s",
            type
        );

        try {
            return typeParser.parse(context, input);
        }catch (Exception ex) {
            return new ExecutionErrorResult(context.command(), ex);
        }
    }
}
