package kboyle.oktane.core.parsers;

import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.TestCommandContext;
import kboyle.oktane.core.exceptions.InvalidResultException;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.TestCommandBuilder;
import kboyle.oktane.core.results.ExecutionErrorResult;
import kboyle.oktane.core.results.Result;
import kboyle.oktane.core.results.argumentparser.FailedArgumentParserResult;
import kboyle.oktane.core.results.argumentparser.SuccessfulArgumentParserResult;
import kboyle.oktane.core.results.typeparser.FailedTypeParserResult;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.stream.Stream;

public class DefaultArgumentParserTests {
    private static final Command COMMAND_INT_ARG_NOT_REMAINDER = new TestCommandBuilder()
        .addParameter(int.class, false)
        .build();

    private static final Command COMMAND_NO_PARAMETERS = new TestCommandBuilder()
        .build();

    private static final Command COMMAND_LONG_ARG_NOT_REMAINDER = new TestCommandBuilder()
        .addParameter(Long.class, false)
        .build();

    private static final Command COMMAND_MISSING_PARAMETER_PARSER = new TestCommandBuilder()
        .addParameter(CommandContext.class, false)
        .build();

    private static final Command COMMAND_STRING_NOT_ARG_REMAINDER = new TestCommandBuilder()
        .addParameter(String.class, false)
        .build();

    private static final Command COMMAND_STRING_ARG_REMAINDER = new TestCommandBuilder()
        .addParameter(String.class, true)
        .build();

    private static final Command COMMAND_STRING_STRING_ARG_REMAINDER = new TestCommandBuilder()
        .addParameter(String.class, false)
        .addParameter(String.class, true)
        .build();

    @Test
    public void testArgumentParserThrowsOnMissingTypeParser() {
        DefaultArgumentParser argumentParser = new DefaultArgumentParser(ImmutableMap.copyOf(PrimitiveTypeParser.DEFAULT_PARSERS));
        Assertions.assertThrows(
            NullPointerException.class,
            () -> argumentParser.parse(new TestCommandContext(COMMAND_MISSING_PARAMETER_PARSER), "string", 0)
        );
    }

    @Test
    public void testArgumentParserThrowsOnBadResult() {
        DefaultArgumentParser argumentParser = new DefaultArgumentParser(ImmutableMap.of(int.class, new BadResultParser()));
        Assertions.assertThrows(
            InvalidResultException.class,
            () -> argumentParser.parse(new TestCommandContext(COMMAND_INT_ARG_NOT_REMAINDER), "string", 0)
        );
    }

    @ParameterizedTest
    @MethodSource("argumentParserTestSource")
    public void argumentParserTest(Command command, String arguments, Result expectedResult) {
        HashMap<Class<?>, TypeParser<?>> parsers = new HashMap<>(PrimitiveTypeParser.DEFAULT_PARSERS);
        parsers.put(Long.class, new BadParser());

        DefaultArgumentParser argumentParser = new DefaultArgumentParser(ImmutableMap.copyOf(parsers));
        Result actualResult = argumentParser.parse(new TestCommandContext(command), arguments, 0);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    public static class BadParser implements TypeParser<Long> {
        @Override
        public TypeParserResult parse(CommandContext context, String input) {
            throw new RuntimeException("Bad Parse");
        }
    }

    public static class BadResultParser implements TypeParser<Integer> {
        @Override
        public TypeParserResult parse(CommandContext context, String input) {
            return new BadResult();
        }
    }

    private static class BadResult implements TypeParserResult {
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    private static Stream<Arguments> argumentParserTestSource() {
        return Stream.of(
            Arguments.of(
                COMMAND_INT_ARG_NOT_REMAINDER,
                "100",
                new SuccessfulArgumentParserResult(new Object[]{ 100 })
            ),
            Arguments.of(
                COMMAND_INT_ARG_NOT_REMAINDER,
                "100 200",
                new FailedArgumentParserResult(COMMAND_INT_ARG_NOT_REMAINDER, FailedArgumentParserResult.Reason.TOO_MANY_ARGUMENTS,  3)
            ),
            Arguments.of(
                COMMAND_INT_ARG_NOT_REMAINDER,
                "",
                new FailedArgumentParserResult(COMMAND_INT_ARG_NOT_REMAINDER, FailedArgumentParserResult.Reason.TOO_FEW_ARGUMENTS, 0)
            ),
            Arguments.of(
                COMMAND_INT_ARG_NOT_REMAINDER,
                "string",
                new FailedTypeParserResult(String.format("Failed to parse %s as %s", "string", int.class))
            ),
            Arguments.of(
                COMMAND_LONG_ARG_NOT_REMAINDER,
                "100" ,
                new ExecutionErrorResult(COMMAND_LONG_ARG_NOT_REMAINDER,  new RuntimeException("Bad Parse"))
            ),
            Arguments.of(
                COMMAND_STRING_NOT_ARG_REMAINDER,
                "string",
                new SuccessfulArgumentParserResult(new Object[]{ "string" })
            ),
            Arguments.of(
                COMMAND_STRING_ARG_REMAINDER,
                "string 123" ,
                new SuccessfulArgumentParserResult(new Object[]{ "string 123" })
            ),
            Arguments.of(
                COMMAND_STRING_STRING_ARG_REMAINDER,
                "string 123 456",
                new SuccessfulArgumentParserResult(new Object[]{ "string", "123 456" })
            ),
            Arguments.of(
                COMMAND_NO_PARAMETERS,
                "",
                new SuccessfulArgumentParserResult(new Object[0])
            ),
            Arguments.of(
                COMMAND_NO_PARAMETERS,
                "          ",
                new SuccessfulArgumentParserResult(new Object[0])
            ),
            Arguments.of(
                COMMAND_INT_ARG_NOT_REMAINDER,
                "10                   ",
                new SuccessfulArgumentParserResult(new Object[]{ 10 })
            ),
            Arguments.of(
                COMMAND_INT_ARG_NOT_REMAINDER,
                "     10         ",
                new SuccessfulArgumentParserResult(new Object[]{ 10 })
            )
        );
    }
}
