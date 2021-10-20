package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.command.TestCommandBuilder;
import com.github.kboyle.oktane.core.mapping.CommandMatch;
import com.github.kboyle.oktane.core.result.Result;
import com.github.kboyle.oktane.core.result.tokeniser.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultTokeniserTests {
    private static final String NULL = null;

    private static final Command INT_ARG_NOT_REMAINDER = new TestCommandBuilder()
        .parameter(int.class, false)
        .build();

    private static final Command NO_PARAMETERS = new TestCommandBuilder()
        .build();

    private static final Command STRING_NOT_ARG_REMAINDER = new TestCommandBuilder()
        .parameter(String.class, false)
        .build();

    private static final Command STRING_ARG_REMAINDER = new TestCommandBuilder()
        .parameter(String.class, true)
        .build();

    private static final Command STRING_STRING_ARG_REMAINDER = new TestCommandBuilder()
        .parameter(String.class, false)
        .parameter(String.class, true)
        .build();

    private static final Command STRING_STRING_NOT_ARG_REMAINDER = new TestCommandBuilder()
        .parameter(String.class, false)
        .parameter(String.class, false)
        .build();

    private static final Command OPTIONAL_STRING = new TestCommandBuilder()
        .optionalParameter(String.class, false)
        .build();

    private static final Command OPTIONAL_STRING_OPTIONAL_STRING = new TestCommandBuilder()
        .optionalParameter(String.class, false)
        .optionalParameter(String.class, false)
        .build();

    private static final Command STRING_OPTIONAL_STRING = new TestCommandBuilder()
        .parameter(String.class, false)
        .optionalParameter(String.class, false)
        .build();

    private static final Command STRING_REMAINDER_OPTIONAL_STRING = new TestCommandBuilder()
        .parameter(String.class, false)
        .optionalParameter(String.class, true)
        .build();

    private static final Command STRING_VARARGS = new TestCommandBuilder()
        .greedy(String.class, false)
        .build();

    private static final Command STRING_VARARGS_REMAINDER = new TestCommandBuilder()
        .greedy(String.class, false, true)
        .build();

    private static final Command STRING_VARARGS_OPTIONAL = new TestCommandBuilder()
        .greedy(String.class, true)
        .build();

    private static final Command INT_GREEDY_STRING = new TestCommandBuilder()
        .greedy(int.class, false)
        .parameter(String.class, false)
        .build();

    private static final Command INT_OPTIONAL_GREEDY_STRING_OPTIONAL = new TestCommandBuilder()
        .greedy(int.class, true)
        .optionalParameter(String.class, false)
        .build();

    @ParameterizedTest(name = "Input: {1}, Result: {2}")
    @MethodSource("argumentParserTestSource")
    void defaultTokeniserReturnsExpectedValue(Command Command, String arguments, Result expectedResult) {
        var tokeniser = new DefaultTokeniser();
        var actualResult = tokeniser.tokenise(arguments, new CommandMatch(Command, 0));
        assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> argumentParserTestSource() {
        return Stream.of(
            Arguments.of(
                INT_ARG_NOT_REMAINDER,
                "c 100",
                new TokeniserSuccessfulResult(List.of("100"))
            ),
            Arguments.of(
                INT_ARG_NOT_REMAINDER,
                "c 100 200",
                new TokeniserTooManyTokensResult("c 100 200", 1)
            ),
            Arguments.of(
                INT_ARG_NOT_REMAINDER,
                "c",
                new TokeniserTooFewTokensResult("c", 1)
            ),
            Arguments.of(
                STRING_NOT_ARG_REMAINDER,
                "c string",
                new TokeniserSuccessfulResult(List.of("string"))
            ),
            Arguments.of(
                STRING_ARG_REMAINDER,
                "c string 123",
                new TokeniserSuccessfulResult(List.of("string 123"))
            ),
            Arguments.of(
                STRING_STRING_ARG_REMAINDER,
                "c string 123 456",
                new TokeniserSuccessfulResult(List.of("string", "123 456"))
            ),
            Arguments.of(
                NO_PARAMETERS,
                "c",
                new TokeniserSuccessfulResult(List.of())
            ),
            Arguments.of(
                NO_PARAMETERS,
                "c          ",
                new TokeniserSuccessfulResult(List.of())
            ),
            Arguments.of(
                INT_ARG_NOT_REMAINDER,
                "c 10                   ",
                new TokeniserSuccessfulResult(List.of("10"))
            ),
            Arguments.of(
                INT_ARG_NOT_REMAINDER,
                "c     10         ",
                new TokeniserSuccessfulResult(List.of("10"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                "c \"this is string one\" \"this is string two\"",
                new TokeniserSuccessfulResult(List.of("this is string one", "this is string two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                "c \"this is string one\" two",
                new TokeniserSuccessfulResult(List.of("this is string one", "two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                "c one \"this is string two\"",
                new TokeniserSuccessfulResult(List.of("one", "this is string two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                "c \"one\" \"two\"",
                new TokeniserSuccessfulResult(List.of("one", "two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                "c \"one\"\"two\"",
                new TokeniserSuccessfulResult(List.of("one", "two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                "c                 \"one\"                    \"two\"                    ",
                new TokeniserSuccessfulResult(List.of("one", "two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                "c \\\"one two\\\"",
                new TokeniserSuccessfulResult(List.of("\"one", "two\""))
            ),
            Arguments.of(
                STRING_NOT_ARG_REMAINDER,
                "c \"missing quote",
                new TokeniserMissingQuoteResult("c \"missing quote", 2)
            ),
            Arguments.of(
                NO_PARAMETERS,
                "c string",
                new TokeniserTooManyTokensResult("c string", 0)
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                "c string ",
                new TokeniserTooFewTokensResult("c string ", 2)
            ),
            Arguments.of(
                STRING_NOT_ARG_REMAINDER,
                "c \"\"",
                new TokeniserSuccessfulResult(List.of(""))
            ),
            Arguments.of(
                STRING_NOT_ARG_REMAINDER,
                "c \\\\",
                new TokeniserSuccessfulResult(List.of("\\"))
            ),
            Arguments.of(
                STRING_NOT_ARG_REMAINDER,
                "c \\\\\\",
                new TokeniserSuccessfulResult(List.of("\\\\"))
            ),
            Arguments.of(
                OPTIONAL_STRING,
                "c",
                new TokeniserSuccessfulResult(Arrays.asList(NULL))
            ),
            Arguments.of(
                OPTIONAL_STRING,
                "c string",
                new TokeniserSuccessfulResult(List.of("string"))
            ),
            Arguments.of(
                OPTIONAL_STRING_OPTIONAL_STRING,
                "c",
                new TokeniserSuccessfulResult(Arrays.asList(NULL, NULL))
            ),
            Arguments.of(
                OPTIONAL_STRING_OPTIONAL_STRING,
                "c string",
                new TokeniserSuccessfulResult(Arrays.asList("string", NULL))
            ),
            Arguments.of(
                OPTIONAL_STRING_OPTIONAL_STRING,
                "c string1 string2",
                new TokeniserSuccessfulResult(List.of("string1", "string2"))
            ),
            Arguments.of(
                STRING_OPTIONAL_STRING,
                "c string",
                new TokeniserSuccessfulResult(Arrays.asList("string", NULL))
            ),
            Arguments.of(
                STRING_OPTIONAL_STRING,
                "c string1 string2",
                new TokeniserSuccessfulResult(List.of("string1", "string2"))
            ),
            Arguments.of(
                STRING_REMAINDER_OPTIONAL_STRING,
                "c string1 string2 string3",
                new TokeniserSuccessfulResult(List.of("string1", "string2 string3"))
            ),
            Arguments.of(
                STRING_REMAINDER_OPTIONAL_STRING,
                "c string1",
                new TokeniserSuccessfulResult(Arrays.asList("string1", NULL))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c string1 string2 string3",
                new TokeniserSuccessfulResult(List.of("string1", "string2", "string3"))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c string1",
                new TokeniserSuccessfulResult(List.of("string1"))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c string1 \"string2 string3\"",
                new TokeniserSuccessfulResult(List.of("string1", "string2 string3"))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c",
                new TokeniserTooFewTokensResult("c", 1)
            ),
            Arguments.of(
                STRING_VARARGS_REMAINDER,
                "c string1 string2 string3",
                new TokeniserSuccessfulResult(List.of("string1 string2 string3"))
            ),
            Arguments.of(
                STRING_VARARGS_REMAINDER,
                "c string1 \"string2 string3\"",
                new TokeniserSuccessfulResult(List.of("string1 \"string2 string3\""))
            ),
            Arguments.of(
                STRING_VARARGS_OPTIONAL,
                "c string1 string2 string3",
                new TokeniserSuccessfulResult(List.of("string1", "string2", "string3"))
            ),
            Arguments.of(
                STRING_VARARGS_OPTIONAL,
                "c",
                new TokeniserSuccessfulResult(Arrays.asList(NULL))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c \"this is string one\" \"this is string two\"",
                new TokeniserSuccessfulResult(List.of("this is string one", "this is string two"))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c \"this is string one\" two",
                new TokeniserSuccessfulResult(List.of("this is string one", "two"))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c one \"this is string two\"",
                new TokeniserSuccessfulResult(List.of("one", "this is string two"))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c \"one\" \"two\"",
                new TokeniserSuccessfulResult(List.of("one", "two"))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c \"one\"\"two\"",
                new TokeniserSuccessfulResult(List.of("one", "two"))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c                 \"one\"                    \"two\"                    ",
                new TokeniserSuccessfulResult(List.of("one", "two"))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c \\\"one two\\\"",
                new TokeniserSuccessfulResult(List.of("\"one", "two\""))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c \"missing quote",
                new TokeniserMissingQuoteResult("c \"missing quote", 2)
            ),
            Arguments.of(
                STRING_VARARGS,
                "c \"\"",
                new TokeniserSuccessfulResult(List.of(""))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c \\\\",
                new TokeniserSuccessfulResult(List.of("\\"))
            ),
            Arguments.of(
                STRING_VARARGS,
                "c \\\\\\",
                new TokeniserSuccessfulResult(List.of("\\\\"))
            ),
            Arguments.of(
                INT_GREEDY_STRING,
                "c 10 string",
                new TokeniserSuccessfulResult(List.of("10", "string"))
            ),
            Arguments.of(
                INT_GREEDY_STRING,
                "c 10 20 30 string",
                new TokeniserSuccessfulResult(List.of("10", "20", "30", "string"))
            ),
            Arguments.of(
                INT_GREEDY_STRING,
                "c 10 20 30",
                new TokeniserSuccessfulResult(List.of("10", "20", "30"))
            ),
            Arguments.of(
                INT_OPTIONAL_GREEDY_STRING_OPTIONAL,
                "c 10 string",
                new TokeniserSuccessfulResult(List.of("10", "string"))
            ),
            Arguments.of(
                INT_OPTIONAL_GREEDY_STRING_OPTIONAL,
                "c 10 20 30 string",
                new TokeniserSuccessfulResult(List.of("10", "20", "30", "string"))
            ),
            Arguments.of(
                INT_OPTIONAL_GREEDY_STRING_OPTIONAL,
                "c 10 20 30",
                new TokeniserSuccessfulResult(List.of("10", "20", "30"))
            ),
            Arguments.of(
                INT_OPTIONAL_GREEDY_STRING_OPTIONAL,
                "c",
                new TokeniserSuccessfulResult(Arrays.asList(NULL, NULL))
            ),
            Arguments.of(
                INT_OPTIONAL_GREEDY_STRING_OPTIONAL,
                "c 10",
                new TokeniserTooFewTokensResult("c 10", 2)
            )
        );
    }
}
