package kboyle.oktane.core.prefix;

import kboyle.oktane.core.ProxyCommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class CharPrefixTests {
    @ParameterizedTest
    @MethodSource("charPrefixTestSource")
    void charPrefixTest(CharPrefix prefix, String input, int expectedOutcome) {
        var context = new ProxyCommandContext(input);
        Assertions.assertEquals(expectedOutcome, prefix.find(context));
    }

    private static Stream<Arguments> charPrefixTestSource() {
        return Stream.of(
            Arguments.of(
                new CharPrefix('!'),
                "!ping",
                1
            ),
            Arguments.of(
                new CharPrefix('!'),
                "ping",
                -1
            ),
            Arguments.of(
                new CharPrefix('!'),
                "! ping",
                1
            )
        );
    }
}
