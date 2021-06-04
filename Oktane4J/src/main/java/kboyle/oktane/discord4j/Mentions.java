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

    public Optional<Snowflake> parseExact(String str) {
        return parseExact(str, 0, str.length() - 1);
    }

    public Optional<Snowflake> parseExact(String str, int startIndex, int endIndex) {
        Preconditions.checkState(startIndex >= 0, "startIndex must be positive");
        Preconditions.checkState(startIndex < endIndex, "endIndex must be greater than startIndex");
        Preconditions.checkState(endIndex < str.length(), "endIndex must not exceed str's last index");

        if (str.length() < 16 || str.charAt(startIndex) != '<' || str.charAt(startIndex + 1) != identifier1) {
            return Optional.empty();
        }

        var left = startIndex + 2;
        if (identifier2 != '\0') {
            var identifier2Present = str.charAt(left) == identifier2;
            if (!identifier2Optional && !identifier2Present) {
                return Optional.empty();
            }

            if (identifier2Present) {
                left++;
            }
        }

        var right = str.indexOf('>', left);
        if (right == -1 || right < endIndex) {
            return Optional.empty();
        }

        var strId = str.substring(left, right);

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
