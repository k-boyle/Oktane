package kboyle.oktane.discord4j.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import reactor.core.publisher.Mono;

/**
 * A base class that extends {@link TypeParser} that restricts the {@link CommandContext} to {@link DiscordCommandContext}.
 *
 * @param <CONTEXT> The type of {@link DiscordCommandContext} to use.
 * @param <T> The type to parse.
 */
public abstract class DiscordTypeParser<CONTEXT extends DiscordCommandContext, T> implements TypeParser<T> {
    @SuppressWarnings("unchecked")
    @Override
    public Mono<TypeParserResult<T>> parse(CommandContext context, Command command, String input) {
        return parse((CONTEXT) context, command, input);
    }

    public abstract Mono<TypeParserResult<T>> parse(CONTEXT context, Command command, String input);
}
