package kboyle.oktane.discord4j;

import discord4j.common.util.Snowflake;

import java.util.Optional;

public enum Snowflakes {
    ;

    public static Optional<Snowflake> parse(String str) {
        try {
            return Optional.of(Snowflake.of(str));
        } catch (NumberFormatException ignore) {
            return Optional.empty();
        }
    }
}
