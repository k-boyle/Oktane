package kboyle.oktane.discord4j.prefix;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.exceptions.InvalidContextTypeException;
import kboyle.oktane.core.prefix.Prefix;
import kboyle.oktane.discord4j.DiscordCommandContext;

public abstract class DiscordPrefix implements Prefix {
    public abstract int find(DiscordCommandContext context);

    @Override
    public int find(CommandContext context) {
        if (context instanceof DiscordCommandContext discordCommandContext) {
            return find(discordCommandContext);
        }

        throw new InvalidContextTypeException(DiscordCommandContext.class, context.getClass());
    }
}
