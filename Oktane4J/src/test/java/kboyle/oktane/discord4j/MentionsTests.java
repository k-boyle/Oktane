package kboyle.oktane.discord4j;

import discord4j.common.util.Snowflake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MentionsTests {
    @ParameterizedTest
    @MethodSource("testMentionsSource")
    public void testMentions(Mentions mention, String input, Optional<Snowflake> expectedSnowflake) {
        var actualSnowflake = mention.parse(input);
        Assertions.assertEquals(expectedSnowflake, actualSnowflake);
    }

    private static Stream<Arguments> testMentionsSource() {
        return Stream.of(
            Arguments.of(
                Mentions.USER,
                "<@84291986575613952>",
                Optional.of(Snowflake.of(84291986575613952L))
            ),
            Arguments.of(
                Mentions.USER,
                "<@!84291986575613952>",
                Optional.of(Snowflake.of(84291986575613952L))
            ),
            Arguments.of(
                Mentions.USER,
                "<@!8429198>",
                Optional.empty()
            ),
            Arguments.of(
                Mentions.USER,
                "@!84291986575613952>",
                Optional.empty()
            ),
            Arguments.of(
                Mentions.USER,
                "<!84291986575613952>",
                Optional.empty()
            ),
            Arguments.of(
                Mentions.USER,
                "<@!84291986575613952",
                Optional.empty()
            ),
            Arguments.of(
                Mentions.USER,
                "<@!8429198657561395>2",
                Optional.empty()
            ),
            Arguments.of(
                Mentions.USER,
                "<@!not a long aaaaaaaaaa>",
                Optional.empty()
            ),
            Arguments.of(
                Mentions.ROLE,
                "<@&443834953336094720>",
                Optional.of(Snowflake.of(443834953336094720L))
            ),
            Arguments.of(
                Mentions.ROLE,
                "<@443834953336094720>",
                Optional.empty()
            ),
            Arguments.of(
                Mentions.CHANNEL,
                "<#443162366360682508>",
                Optional.of(Snowflake.of(443162366360682508L))
            )
        );
    }
}
