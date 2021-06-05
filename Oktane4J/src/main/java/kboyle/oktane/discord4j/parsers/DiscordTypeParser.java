package kboyle.oktane.discord4j.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.exceptions.InvalidContextTypeException;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import reactor.core.publisher.Mono;

/**
 * A base class that extends {@link TypeParser} that restricts the {@link CommandContext} to {@link DiscordCommandContext}.
 *
 * @param <T> The type to parse.
 */
public abstract class DiscordTypeParser<T> implements TypeParser<T> {
    @Override
    public Mono<TypeParserResult<T>> parse(CommandContext context, Command command, String input) {
        if (context instanceof DiscordCommandContext discordCommandContext) {
            return parse(discordCommandContext, command, input);
        }

        throw new InvalidContextTypeException(DiscordCommandContext.class, context.getClass());
    }

    public abstract Mono<TypeParserResult<T>> parse(DiscordCommandContext context, Command command, String input);
}
