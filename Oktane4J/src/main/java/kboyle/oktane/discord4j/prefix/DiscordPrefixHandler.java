package kboyle.oktane.discord4j.prefix;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.PrefixHandler;
import kboyle.oktane.core.exceptions.InvalidContextTypeException;
import kboyle.oktane.core.prefix.Prefix;
import kboyle.oktane.discord4j.DiscordCommandContext;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface DiscordPrefixHandler extends PrefixHandler {
    Mono<Collection<Prefix>> get(DiscordCommandContext context);

    @Override
    default Mono<Collection<Prefix>> get(CommandContext context) {
        if (context instanceof DiscordCommandContext discordCommandContext) {
            return get(discordCommandContext);
        }

        throw new InvalidContextTypeException(DiscordCommandContext.class, context.getClass());
    }
}
