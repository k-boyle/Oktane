package kboyle.oktane.reactive.parsers;

import kboyle.oktane.reactive.mapping.CommandMatch;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.TestReactiveCommandBuilder;
import kboyle.oktane.reactive.results.Result;
import kboyle.oktane.reactive.results.tokeniser.TokeniserMissingQuoteResult;
import kboyle.oktane.reactive.results.tokeniser.TokeniserSuccessfulResult;
import kboyle.oktane.reactive.results.tokeniser.TokeniserTooFewTokensResult;
import kboyle.oktane.reactive.results.tokeniser.TokeniserTooManyTokensResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

public class DefaultTokeniserTests {
    private static final ReactiveCommand INT_ARG_NOT_REMAINDER = new TestReactiveCommandBuilder()
        .addParameter(int.class, false)
        .build();

    private static final ReactiveCommand NO_PARAMETERS = new TestReactiveCommandBuilder()
        .build();

    private static final ReactiveCommand STRING_NOT_ARG_REMAINDER = new TestReactiveCommandBuilder()
        .addParameter(String.class, false)
        .build();

    private static final ReactiveCommand STRING_ARG_REMAINDER = new TestReactiveCommandBuilder()
        .addParameter(String.class, true)
        .build();

    private static final ReactiveCommand STRING_STRING_ARG_REMAINDER = new TestReactiveCommandBuilder()
        .addParameter(String.class, false)
        .addParameter(String.class, true)
        .build();

    private static final ReactiveCommand STRING_STRING_NOT_ARG_REMAINDER = new TestReactiveCommandBuilder()
        .addParameter(String.class, false)
        .addParameter(String.class, false)
        .build();

    @ParameterizedTest
    @MethodSource("argumentParserTestSource")
    public void argumentParserTest(ReactiveCommand ReactiveCommand, String arguments, Result expectedResult) {
        DefaultTokeniser tokeniser = new DefaultTokeniser();
        Result actualResult = tokeniser.tokenise(arguments, new CommandMatch(ReactiveCommand, 0, 0, 1));
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> argumentParserTestSource() {
        return Stream.of(
            Arguments.of(
                INT_ARG_NOT_REMAINDER,
                " 100",
                new TokeniserSuccessfulResult(INT_ARG_NOT_REMAINDER, List.of("100"))
            ),
            Arguments.of(
                INT_ARG_NOT_REMAINDER,
                " 100 200",
                new TokeniserTooManyTokensResult(INT_ARG_NOT_REMAINDER, " 100 200", 1)
            ),
            Arguments.of(
                INT_ARG_NOT_REMAINDER,
                " ",
                new TokeniserTooFewTokensResult(INT_ARG_NOT_REMAINDER, " ", 1)
            ),
            Arguments.of(
                STRING_NOT_ARG_REMAINDER,
                " string",
                new TokeniserSuccessfulResult(STRING_NOT_ARG_REMAINDER, List.of("string"))
            ),
            Arguments.of(
                STRING_ARG_REMAINDER,
                " string 123",
                new TokeniserSuccessfulResult(STRING_ARG_REMAINDER, List.of("string 123"))
            ),
            Arguments.of(
                STRING_STRING_ARG_REMAINDER,
                " string 123 456",
                new TokeniserSuccessfulResult(STRING_STRING_ARG_REMAINDER, List.of("string", "123 456"))
            ),
            Arguments.of(
                NO_PARAMETERS,
                " ",
                new TokeniserSuccessfulResult(NO_PARAMETERS, List.of())
            ),
            Arguments.of(
                NO_PARAMETERS,
                "          ",
                new TokeniserSuccessfulResult(NO_PARAMETERS, List.of())
            ),
            Arguments.of(
                INT_ARG_NOT_REMAINDER,
                " 10                   ",
                new TokeniserSuccessfulResult(INT_ARG_NOT_REMAINDER, List.of("10"))
            ),
            Arguments.of(
                INT_ARG_NOT_REMAINDER,
                "     10         ",
                new TokeniserSuccessfulResult(INT_ARG_NOT_REMAINDER, List.of("10"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                " \"this is string one\" \"this is string two\"",
                new TokeniserSuccessfulResult(STRING_STRING_NOT_ARG_REMAINDER, List.of("this is string one", "this is string two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                " \"this is string one\" two",
                new TokeniserSuccessfulResult(STRING_STRING_NOT_ARG_REMAINDER, List.of("this is string one", "two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                " one \"this is string two\"",
                new TokeniserSuccessfulResult(STRING_STRING_NOT_ARG_REMAINDER, List.of("one", "this is string two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                " \"one\" \"two\"",
                new TokeniserSuccessfulResult(STRING_STRING_NOT_ARG_REMAINDER, List.of("one", "two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                " \"one\"\"two\"",
                new TokeniserSuccessfulResult(STRING_STRING_NOT_ARG_REMAINDER, List.of("one", "two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                "                 \"one\"                    \"two\"                    ",
                new TokeniserSuccessfulResult(STRING_STRING_NOT_ARG_REMAINDER, List.of("one", "two"))
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                " \\\"one two\\\"",
                new TokeniserSuccessfulResult(STRING_STRING_NOT_ARG_REMAINDER, List.of("\"one", "two\\\""))
            ),
            Arguments.of(
                STRING_NOT_ARG_REMAINDER,
                " \"missing quote",
                new TokeniserMissingQuoteResult(STRING_NOT_ARG_REMAINDER, " \"missing quote", 15)
            ),
            Arguments.of(
                NO_PARAMETERS,
                " string",
                new TokeniserTooManyTokensResult(NO_PARAMETERS, " string", 0)
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                " string ",
                new TokeniserTooFewTokensResult(STRING_STRING_NOT_ARG_REMAINDER, " string ", 2)
            ),
            Arguments.of(
                STRING_NOT_ARG_REMAINDER,
                " \"\"",
                new TokeniserSuccessfulResult(STRING_NOT_ARG_REMAINDER, List.of(""))
            ),
            Arguments.of(
                STRING_NOT_ARG_REMAINDER,
                " \\\\",
                new TokeniserSuccessfulResult(STRING_NOT_ARG_REMAINDER, List.of("\\"))
            ),
            Arguments.of(
                STRING_NOT_ARG_REMAINDER,
                " \\\\\\",
                new TokeniserSuccessfulResult(STRING_NOT_ARG_REMAINDER, List.of("\\\\"))
            )
        );
    }
}
