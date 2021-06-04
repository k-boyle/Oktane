package kboyle.oktane.discord4j.prefix;

import discord4j.common.util.Snowflake;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.Mentions;

import java.util.Objects;
import java.util.function.BiPredicate;

public class MentionPrefix extends DiscordPrefix {
    private static class SingletonHolder {
        private static final MentionPrefix INSTANCE = new MentionPrefix();
    }

    private final BiPredicate<Snowflake, Snowflake> snowflakePredicate;
    private Snowflake snowflake;

    public MentionPrefix(Snowflake snowflake) {
        this.snowflakePredicate = (left, right) -> left.equals(snowflake);
        this.snowflake = snowflake;
    }

    public MentionPrefix() {
        this.snowflakePredicate = Snowflake::equals;
    }

    @Override
    public int find(DiscordCommandContext context) {
        var message = context.message();
        var content = message.getContent();
        var close = content.indexOf('>');
        if (content.charAt(0) != '<' || close == -1) {
            return -1;
        }

        return Mentions.USER.parseExact(content, 0,  close)
            .filter(snowflake -> snowflakePredicate.test(snowflake, message.getClient().getSelfId()))
            .map(snowflake -> close + 1)
            .orElse(-1);
    }

    @Override
    public Object value() {
        return String.format("<@%d>", snowflake == null ? -1 : snowflake.asLong());
    }

    public static MentionPrefix bot() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof MentionPrefix other && Objects.equals(snowflake, other.snowflake);
    }

    @Override
    public int hashCode() {
        return snowflake != null ? snowflake.hashCode() : -1;
    }
}
