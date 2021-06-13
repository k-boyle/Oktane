package kboyle.oktane.core.parsers;

import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.ProxyCommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.TestCommandBuilder;
import kboyle.oktane.core.results.argumentparser.ArgumentParserFailedResult;
import kboyle.oktane.core.results.argumentparser.ArgumentParserResult;
import kboyle.oktane.core.results.argumentparser.ArgumentParserSuccessfulResult;
import kboyle.oktane.core.results.typeparser.TypeParserFailedResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

class DefaultArgumentParserTests {
    private static final Command INT_ARG = new TestCommandBuilder()
        .addParameter(int.class, false)
        .build();

    private static final Command NO_PARAMETERS = new TestCommandBuilder()
        .build();

    private static final Command STRING_STRING_NOT_ARG_REMAINDER = new TestCommandBuilder()
        .addParameter(String.class, false)
        .addParameter(String.class, false)
        .build();

    private static final Command OPTIONAL_STRING_NO_DEFAULT = new TestCommandBuilder()
        .addOptionalParameter(String.class, null)
        .build();

    private static final Command OPTIONAL_STRING_DEFAULT = new TestCommandBuilder()
        .addOptionalParameter(String.class, "default token")
        .build();

    private static final Command OPTIONAL_INT_NO_DEFAULT = new TestCommandBuilder()
        .addOptionalParameter(int.class, null)
        .build();

    private static final Command OPTIONAL_INT_DEFAULT = new TestCommandBuilder()
        .addOptionalParameter(int.class, "20")
        .build();

    private static final CommandContext CONTEXT = new ProxyCommandContext();

    private static final ImmutableMap<Class<?>, TypeParser<?>> TYPE_PARSERS = ImmutableMap.<Class<?>, TypeParser<?>>builder()
        .put(int.class, new PrimitiveTypeParser<>(Integer.class, Integer::parseInt))
        .build();

    @ParameterizedTest
    @MethodSource("argumentParserTestSource")
    void testArgumentParser(Command command, List<String> tokens, ArgumentParserResult expectedResult) {
        var parser = new DefaultArgumentParser(TYPE_PARSERS);
        var actualResult = parser.parse(CONTEXT, command, tokens);
        Assertions.assertEquals(expectedResult, actualResult.block());
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
                new ArgumentParserFailedResult(INT_ARG.parameters.get(0), "notint", new TypeParserFailedResult<>("Failed to parse notint as class java.lang.Integer"))
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
            )
        );
    }
}
