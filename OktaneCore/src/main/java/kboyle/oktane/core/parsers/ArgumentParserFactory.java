package kboyle.oktane.core.parsers;

import kboyle.oktane.core.exceptions.UnhandledTypeException;
import kboyle.oktane.core.module.CommandParameter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArgumentParserFactory {
    private final Map<Class<?>, TypeParser<?>> typeParserByClass;

    public ArgumentParserFactory(Map<Class<?>, TypeParser<?>> typeParserByClass) {
        this.typeParserByClass = typeParserByClass;
    }

    /*
    public CommandResult add(int a, int b) {
        return number(a + b);
    }


    public class AddArgumentParser implements ArgumentParser {
        private static final char SPACE = ' ';
        private static final char QUOTE = '"';
        private static final char ESCAPE = '\\';
        private static final String EMPTY = "";

        private final TypeParser<Integer> arg0;
        private final TypeParser<Integer> arg1;
        private final Command command;

        public AddArgumentParser(Command command, ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass) {
            this.command = command;
            this.arg0 = notnull(get);
            this.arg1 = notnull(get);
        }

        @Override
        public ArgumentParserResult parse(CommandContext context, CommandMatch commandMatch, String input) {
            int index = commandMatch.argumentStart();
            int commandEnd = commandMatch.commandEnd();

            if (index == commandEnd) {
                return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_FEW_ARGUMENTS, index);
            }

            int p0i = nextParameterIndex(input, index);
            Integer p0;

            if (p1i == input.length() - 1) {
                return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_FEW_ARGUMENTS, index);
            }

           String currentParameter = null;
           int paramStart = index;
           for (; index < inputLength; index++) {
               char currentCharacter = input.charAt(index);

               if (currentCharacter == QUOTE) {
                   if (input.charAt(index - 1) == ESCAPE) {
                       continue;
                   }

                   index++;
                   for (; index < inputLength; index++) {
                       if (input.charAt(index) != QUOTE) {
                           continue;
                        }

                        if (input.charAt(index - 1) != ESCAPE) {
                            currentParameter = paramStart + 1 == index
                                ? EMPTY
                                : input.substring(paramStart + 1, index + 1);

                            break;
                        }
                    }

                    if (index >= inputLastIndex) {
                        return new ArgumentParserFailedResult(command, ParserFailedReason.MISSING_QUOTE, index);
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

            try {
                TypeParserResult<Integer> result = arg0.parse(context, input);
                if (result.success()) {
                    p0 = result.value();
                } else {
                    return new ArgumentParserFailedToParseArgumentResult(result);
                }
            } catch (Exception ex) {
                return new ArgumentParserExceptionResult(command, ex);
            }

            int p1i = nextParameterIndex(input, index);
            Integer p1;

            int paramStart = index;
            for (; index < inputLength; index++) {
                char currentCharacter = input.charAt(index);

                if (currentCharacter == QUOTE) {
                    if (input.charAt(index - 1) == ESCAPE) {
                        continue;
                    }

                    index++;
                    for (; index < inputLength; index++) {
                        if (input.charAt(index) != QUOTE) {
                            continue;
                         }

                         if (input.charAt(index - 1) != ESCAPE) {
                             currentParameter = paramStart + 1 == index
                                 ? EMPTY
                                 : input.substring(paramStart + 1, index + 1);

                             break;
                         }
                     }

                     if (index >= inputLastIndex) {
                         return new ArgumentParserFailedResult(command, ParserFailedReason.MISSING_QUOTE, index);
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

             try {
                TypeParserResult<Integer> result = arg1.parse(context, input);
                if (result.success()) {
                    p1 = result.value();
                } else {
                    return new ArgumentParserFailedToParseArgumentResult(result);
                }
            } catch (Exception ex) {
                return new ArgumentParserExceptionResult(context.command(), ex);
            }

            if (index != inputLength && noneWhitespaceRemains(input, index)) {
                return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_MANY_ARGUMENTS, index);
            }

            // later I can just execute the command from here to
            // - 1 not allocate this result
            // - 2 not have to allocate and unwrap the object array
            return new ArgumentParserSuccessfulResult(new Object[] { p1, p2 });
        }

        private int nextParameterIndex(String input, int startIndex) {
            for (; startIndex < inputLength; startIndex++) {
                char currentCharacter = input.charAt(startIndex);
                if (currentCharacter == SPACE) {
                    continue;
                }

                if (currentCharacter != ESCAPE || input.charAt(startIndex - 1) == ESCAPE) {
                    break;
                }
            }

            return startIndex;
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
     */

    private static final String NAME_TEMPLATE = "%sArgumentParser%d";

    public ArgumentParser create(Method method, List<CommandParameter> commandParameters) {
        List<Class<?>> parameterTypes = commandParameters.stream()
            .map(CommandParameter::type)
            .collect(Collectors.toList());

        Type returnType = method.getGenericReturnType();


        return null;
    }

    private List<Class<?>> getUsedTypes(Type type, List<Class<?>> types) {
        if (type instanceof Class<?> clazz) {
            types.add(clazz);
            return types;
        } else if (type instanceof ParameterizedType parameterizedType) {

        }

        throw new UnhandledTypeException(type);
    }
}
