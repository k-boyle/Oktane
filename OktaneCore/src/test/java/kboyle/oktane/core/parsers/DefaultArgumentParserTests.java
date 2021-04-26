package kboyle.oktane.core.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.TestCommandContext;
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

public class DefaultArgumentParserTests {
    private static final Command INT_ARG = new TestCommandBuilder()
        .addParameter(int.class, false)
        .build();

    private static final Command NO_PARAMETERS = new TestCommandBuilder()
        .build();

    private static final Command STRING_STRING_NOT_ARG_REMAINDER = new TestCommandBuilder()
        .addParameter(String.class, false)
        .addParameter(String.class, false)
        .build();

    private static final CommandContext CONTEXT = new TestCommandContext();

    @ParameterizedTest
    @MethodSource("argumentParserTestSource")
    public void testArgumentParser(Command command, List<String> tokens, ArgumentParserResult expectedResult) {
        var parser = new DefaultArgumentParser(PrimitiveTypeParserFactory.create());
        var actualResult = parser.parse(CONTEXT, command, tokens);
        Assertions.assertEquals(expectedResult, actualResult.block());
    }

    private static Stream<Arguments> argumentParserTestSource() {
        return Stream.of(
            Arguments.of(
                INT_ARG,
                List.of("1"),
                new ArgumentParserSuccessfulResult(INT_ARG, new Object[] { 1 })
            ),
            Arguments.of(
                NO_PARAMETERS,
                List.of(""),
                new ArgumentParserSuccessfulResult(NO_PARAMETERS, new Object[0])
            ),
            Arguments.of(
                STRING_STRING_NOT_ARG_REMAINDER,
                List.of("a", "b"),
                new ArgumentParserSuccessfulResult(STRING_STRING_NOT_ARG_REMAINDER, new Object[] { "a", "b" })
            ),
            Arguments.of(
                INT_ARG,
                List.of("notint"),
                new ArgumentParserFailedResult(INT_ARG, List.of(new TypeParserFailedResult<Integer>("Failed to parse notint as class java.lang.Integer")))
            )
        );
    }
}
