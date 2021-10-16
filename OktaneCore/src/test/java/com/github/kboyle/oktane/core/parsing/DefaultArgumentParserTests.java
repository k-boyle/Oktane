package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.command.TestCommandBuilder;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.argumentparser.*;
import com.github.kboyle.oktane.core.result.typeparser.TypeParserFailResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

class DefaultArgumentParserTests {
    private static final TypeParser<Integer> INTEGER_TYPE_PARSER = TypeParser.simple(Integer.class, Integer::parseInt);

    private static final Command INT_ARG = new TestCommandBuilder()
        .parameter(INTEGER_TYPE_PARSER)
        .build();

    private static final Command NO_PARAMETERS = new TestCommandBuilder()
        .build();

    private static final Command STRING_STRING_NOT_ARG_REMAINDER = new TestCommandBuilder()
        .parameter(String.class, false)
        .parameter(String.class, false)
        .build();

    private static final Command OPTIONAL_STRING_NO_DEFAULT = new TestCommandBuilder()
        .optionalParameter(String.class, false)
        .build();

    private static final Command OPTIONAL_STRING_DEFAULT = new TestCommandBuilder()
        .optionalParameter(String.class, "default token")
        .build();

    private static final Command OPTIONAL_INT_NO_DEFAULT = new TestCommandBuilder()
        .optionalParameter(int.class, false)
        .build();

    private static final Command OPTIONAL_INT_DEFAULT = new TestCommandBuilder()
        .optionalParameter(int.class, "20")
        .build();

    private static final Command VARARGS_STRING = new TestCommandBuilder()
        .varargs(String.class)
        .build();

    private static final Command STRING_VARARGS_STRING = new TestCommandBuilder()
        .parameter(String.class, false)
        .varargs(String.class)
        .build();

    private static final Command STRING_VARARGS_INT = new TestCommandBuilder()
        .parameter(String.class, false)
        .varargs(int.class, INTEGER_TYPE_PARSER)
        .build();

    private static final Command STRING_VARARGS_DEFAULT = new TestCommandBuilder()
        .varargs(String.class) // todo
        .build();

    @ParameterizedTest
    @MethodSource("argumentParserTestSource")
    void testArgumentParser(Command command, List<String> tokens, ArgumentParserResult expectedResult) {
        var parser = new DefaultArgumentParser();
        var actualResult = parser.parse(new CommandContext(), command, tokens);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> argumentParserTestSource() {
        return Stream.of(
            Arguments.of(
                INT_ARG,
                List.of("1"),
                new ArgumentParserSuccessfulResult(new Object[] { 1 })
            ),
            Arguments.of(
                NO_PARAMETERS,
                List.of(""),
                new ArgumentParserSuccessfulResult(new Object[0])
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                List.of("a", "b"),
                new ArgumentParserSuccessfulResult(new Object[] { "a", "b" })
            ),
            Arguments.of(
                INT_ARG,
                List.of("notint"),
                new ArgumentParserTypeParserFailResult(INT_ARG.parameters().get(0), "notint", new TypeParserFailResult<>(INTEGER_TYPE_PARSER, "Failed to parse notint as Integer"))
            ),
            Arguments.of(
                OPTIONAL_STRING_NO_DEFAULT,
                List.of(),
                new ArgumentParserSuccessfulResult(new Object[] { null })
            ),
            Arguments.of(
                OPTIONAL_STRING_NO_DEFAULT,
                List.of("string"),
                new ArgumentParserSuccessfulResult(new Object[] { "string" })
            ),
            Arguments.of(
                OPTIONAL_STRING_DEFAULT,
                List.of(),
                new ArgumentParserSuccessfulResult(new Object[] { "default token" })
            ),
            Arguments.of(
                OPTIONAL_STRING_DEFAULT,
                List.of("string"),
                new ArgumentParserSuccessfulResult(new Object[] { "string" })
            ),
            Arguments.of(
                OPTIONAL_INT_NO_DEFAULT,
                List.of(),
                new ArgumentParserSuccessfulResult(new Object[] { 0 })
            ),
            Arguments.of(
                OPTIONAL_INT_NO_DEFAULT,
                List.of("10"),
                new ArgumentParserSuccessfulResult(new Object[] { 10 })
            ),
            Arguments.of(
                OPTIONAL_INT_DEFAULT,
                List.of(),
                new ArgumentParserSuccessfulResult(new Object[] { 20 })
            ),
            Arguments.of(
                OPTIONAL_INT_DEFAULT,
                List.of("10"),
                new ArgumentParserSuccessfulResult(new Object[] { 10 })
            ),
            Arguments.of(
                VARARGS_STRING,
                List.of("a"),
                new ArgumentParserSuccessfulResult(new Object[] { new String[] { "a" } })
            ),
            Arguments.of(
                VARARGS_STRING,
                List.of("a", "b"),
                new ArgumentParserSuccessfulResult(new Object[] { new String[] { "a", "b" } })
            ),
            Arguments.of(
                STRING_VARARGS_STRING,
                List.of("a", "b"),
                new ArgumentParserSuccessfulResult(new Object[] { "a", new String[] { "b" } })
            ),
            Arguments.of(
                STRING_VARARGS_STRING,
                List.of("a", "b"),
                new ArgumentParserSuccessfulResult(new Object[] { "a", new String[] { "b" } })
            ),
            Arguments.of(
                STRING_VARARGS_STRING,
                List.of("a", "b", "c"),
                new ArgumentParserSuccessfulResult(new Object[] { "a", new String[] { "b", "c" } })
            ),
            Arguments.of(
                STRING_VARARGS_INT,
                List.of("a", "10", "20"),
                new ArgumentParserSuccessfulResult(new Object[] { "a", new int[] { 10, 20 } })
            ),
            Arguments.of(
                STRING_VARARGS_INT,
                List.of("a", "notint"),
                new ArgumentParserTypeParserFailResult(STRING_VARARGS_INT.parameters().get(1), "notint", new TypeParserFailResult<>(INTEGER_TYPE_PARSER, "Failed to parse notint as Integer"))
            ),
            Arguments.of(
                STRING_VARARGS_INT,
                List.of("a", "10", "notint"),
                new ArgumentParserTypeParserFailResult(STRING_VARARGS_INT.parameters().get(1), "notint", new TypeParserFailResult<>(INTEGER_TYPE_PARSER, "Failed to parse notint as Integer"))
            ),
            Arguments.of(
                STRING_VARARGS_INT,
                List.of("a", "notint", "10"),
                new ArgumentParserTypeParserFailResult(STRING_VARARGS_INT.parameters().get(1), "notint", new TypeParserFailResult<>(INTEGER_TYPE_PARSER, "Failed to parse notint as Integer"))
            )
        );
    }
}
