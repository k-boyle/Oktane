package kboyle.oktane.discord4j.prefix;

import kboyle.oktane.core.prefix.Prefix;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.Mentions;

public class MentionPrefix<CONTEXT extends DiscordCommandContext> implements Prefix<CONTEXT> {
    @Override
    public int find(CONTEXT context) {
        var message = context.message();
        var content = message.getContent();
        var firstSpace = content.indexOf(' ');
        if (firstSpace == -1 || firstSpace == content.length() - 1) {
            return -1;
        }

        return Mentions.USER.parse(content, 0, firstSpace - 1)
            .filter(snowflake -> snowflake.equals(message.getClient().getSelfId()))
            .map(snowflake -> firstSpace + 1)
            .orElse(-1);
    }
}
