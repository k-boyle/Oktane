package kboyle.oktane.discord4j.parsers;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.Mentions;
import kboyle.oktane.discord4j.Snowflakes;
import reactor.core.publisher.Mono;

/**
 * A {@link DiscordTypeParser} for parsing Discord {@link Channel}'s.
 *
 * @param <CHANNEL> The type of {@link Channel} to parse.
 */
public class ChannelTypeParser<CHANNEL extends Channel> extends DiscordTypeParser<CHANNEL> {
    private final Class<CHANNEL> channelClass;

    public ChannelTypeParser(Class<CHANNEL> channelClass) {
        this.channelClass = channelClass;
    }

    @Override
    public Mono<TypeParserResult<CHANNEL>> parse(DiscordCommandContext context, Command command, String input) {
        var channelId = Mentions.CHANNEL.parseExact(input)
            .or(() -> Snowflakes.parse(input));

        return channelId.map(id -> context.client().getChannelById(id))
            .orElseGet(() ->
                context.guild()
                    .flatMapMany(Guild::getChannels)
                    .filter(channel -> channel.getName().equalsIgnoreCase(input))
                    .next()
                    .cast(Channel.class)
            )
            .ofType(channelClass)
            .map(this::success)
            .switchIfEmpty(failure("Failed to find a channel matching %s, try mentioning it", input).mono());
    }
}
