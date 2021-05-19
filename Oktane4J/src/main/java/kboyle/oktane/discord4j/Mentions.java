package kboyle.oktane.discord4j;

import com.google.common.base.Preconditions;
import discord4j.common.util.Snowflake;

import java.util.Optional;

public enum Mentions {
    USER('@', '!', true),
    ROLE('@', '&', false),
    CHANNEL('#', '\0', false),
    ;

    private final char identifier1;
    private final char identifier2;
    private final boolean identifier2Optional;

    Mentions(char identifier1, char identifier2, boolean identifier2Optional) {
        this.identifier1 = identifier1;
        this.identifier2 = identifier2;
        this.identifier2Optional = identifier2Optional;
    }

    public Optional<Snowflake> parse(String str) {
        return parse(str, 0, str.length() - 1);
    }

    public Optional<Snowflake> parseFrom(String str, int startIndex) {
        return parse(str, startIndex, str.length() - 1);
    }

    public Optional<Snowflake> parseTo(String str, int endIndex) {
        return parse(str, 0, endIndex);
    }

    public Optional<Snowflake> parse(String str, int startIndex, int endIndex) {
        Preconditions.checkState(startIndex >= 0, "startIndex must be positive");
        Preconditions.checkState(startIndex < endIndex, "endIndex must be greater than startIndex");
        Preconditions.checkState(endIndex < str.length(), "endIndex must not exceed str's last index");

        if (str.length() < 16 || str.charAt(startIndex) != '<' || str.charAt(startIndex + 1) != identifier1) {
            return Optional.empty();
        }

        var open = startIndex + 2;
        if (identifier2 != '\0') {
            var identifier2Present = str.charAt(startIndex + 2) == identifier2;
            if (!identifier2Optional && !identifier2Present) {
                return Optional.empty();
            }

            if (identifier2Present) {
                open++;
            }
        }

        var close = str.lastIndexOf('>');
        if (close == -1 || close != str.length() - 1 || close > endIndex) {
            return Optional.empty();
        }

        var strId = str.substring(open, close);

        try {
            return Optional.of(Snowflake.of(strId));
        } catch (NumberFormatException ignore) {
            return Optional.empty();
        }
    }

    public String mention(Snowflake id) {
        return String.format("<%s%s%d>", identifier1, identifier2, id.asLong());
    }
}
