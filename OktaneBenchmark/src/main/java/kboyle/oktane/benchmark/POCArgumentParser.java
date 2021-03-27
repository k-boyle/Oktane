package kboyle.oktane.benchmark;

import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.parsers.ArgumentParser;
import kboyle.oktane.core.parsers.ParserFailedReason;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.results.argumentparser.ArgumentParserFailedResult;
import kboyle.oktane.core.results.argumentparser.ArgumentParserResult;
import kboyle.oktane.core.results.argumentparser.ArgumentParserSuccessfulResult;

public class POCArgumentParser implements ArgumentParser {
    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE = '\\';
    private static final String EMPTY = "";

    private final Command command;

    public POCArgumentParser(Command command, ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass) {
        this.command = command;
    }

    @Override
    public ArgumentParserResult parse(CommandContext context, CommandMatch commandMatch, String input) {
        int index = commandMatch.argumentStart();
        int commandEnd = commandMatch.commandEnd();

        if (index == commandEnd) {
            return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_FEW_ARGUMENTS, index);
        }

        int inputLength = input.length();
        int p0i = nextParameterIndex(input, inputLength, index);
        String p0;

        if (p0i == input.length() - 1) {
            return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_FEW_ARGUMENTS, index);
        }

        String currentParameter = null;
        int paramStart = p0i;
        index = p0i;
        int inputLastIndex = inputLength - 1;
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

        p0 = currentParameter;

        int p1i = nextParameterIndex(input, inputLength, index);
        String p1;

        paramStart = p1i;
        index = p1i;
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

        p1 = currentParameter;

        int p2i = nextParameterIndex(input, inputLength, index);
        String p2;

        paramStart = p2i;
        index = p2i;
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

        p2 = currentParameter;

        int p3i = nextParameterIndex(input, inputLength, index);
        String p3;

        paramStart = p3i;
        index = p3i;
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

        p3 = currentParameter;

        int p4i = nextParameterIndex(input, inputLength, index);
        String p4;

        paramStart = p4i;
        index = p4i;
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

        p4 = currentParameter;

        int p5i = nextParameterIndex(input, inputLength, index);
        String p5;

        paramStart = p5i;
        index = p5i;
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

        p5 = currentParameter;

        int p6i = nextParameterIndex(input, inputLength, index);
        String p6;

        paramStart = p6i;
        index = p6i;
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

        p6 = currentParameter;

        if (index != inputLength && noneWhitespaceRemains(input, index)) {
            return new ArgumentParserFailedResult(command, ParserFailedReason.TOO_MANY_ARGUMENTS, index);
        }

        // later I can just execute the command from here to
        // - 1 not allocate this result
        // - 2 not have to allocate and unwrap the object array
        return new ArgumentParserSuccessfulResult(new Object[] { p0, p1, p2, p3, p4, p5, p6 });
    }

    private int nextParameterIndex(String input, int inputLength, int startIndex) {
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
