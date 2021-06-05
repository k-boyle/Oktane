package kboyle.oktane.discord4j;

import discord4j.common.util.Snowflake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SnowflakesTests {
    @Test
    void testCorrectSnowflakeIsParsed() {
        var originalStr = "84291986575613952";
        var parsed = Snowflakes.parse(originalStr);
        Assertions.assertTrue(parsed.isPresent());
        Assertions.assertEquals(Snowflake.of(84291986575613952L), parsed.get());
    }

    @Test
    void testSnowflakesReturnsEmptyOnInvalidString() {
        var originalStr = "not a long";
        var parsed = Snowflakes.parse(originalStr);
        Assertions.assertTrue(parsed.isEmpty());
    }
}
