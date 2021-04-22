package kboyle.oktane.reactive.parsers;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.TestCommandContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.TestReactiveCommandBuilder;
import kboyle.oktane.reactive.results.argumentparser.ArgumentParserFailedResult;
import kboyle.oktane.reactive.results.argumentparser.ArgumentParserResult;
import kboyle.oktane.reactive.results.argumentparser.ArgumentParserSuccessfulResult;
import kboyle.oktane.reactive.results.typeparser.TypeParserFailedResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Stream;

public class DefaultArgumentParserTests {
    private static final ReactiveCommand INT_ARG = new TestReactiveCommandBuilder()
        .addParameter(int.class, false)
        .build();

    private static final ReactiveCommand NO_PARAMETERS = new TestReactiveCommandBuilder()
        .build();

    private static final ReactiveCommand STRING_STRING_NOT_ARG_REMAINDER = new TestReactiveCommandBuilder()
        .addParameter(String.class, false)
        .addParameter(String.class, false)
        .build();

    private static final CommandContext CONTEXT = new TestCommandContext();

    @ParameterizedTest
    @MethodSource("argumentParserTestSource")
    public void testArgumentParser(ReactiveCommand command, List<String> tokens, ArgumentParserResult expectedResult) {
        DefaultArgumentParser parser = new DefaultArgumentParser(PrimitiveReactiveTypeParserFactory.create());
        Mono<ArgumentParserResult> actualResult = parser.parse(CONTEXT, command, tokens);
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
