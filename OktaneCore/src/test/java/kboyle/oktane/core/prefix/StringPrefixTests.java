package kboyle.oktane.core.prefix;

import kboyle.oktane.core.ProxyCommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class StringPrefixTests {
    @ParameterizedTest
    @MethodSource("stringPrefixTestSource")
    public void stringPrefixTest(StringPrefix prefix, String input, int expectedOutcome) {
        var context = new ProxyCommandContext(input);
        Assertions.assertEquals(expectedOutcome, prefix.find(context));
    }

    private static Stream<Arguments> stringPrefixTestSource() {
        return Stream.of(
            Arguments.of(
                new StringPrefix("!"),
                "!ping",
                1
            ),
            Arguments.of(
                new StringPrefix("!"),
                "ping",
                -1
            ),
            Arguments.of(
                new StringPrefix("prefix"),
                "prefixping",
                6
            ),
            Arguments.of(
                new StringPrefix("prefix"),
                "prefix ping",
                6
            )
        );
    }
}
