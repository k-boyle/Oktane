package kboyle.oktane.discord4j.parsers;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.Mentions;
import kboyle.oktane.discord4j.Snowflakes;
import reactor.core.publisher.Mono;

public class UserTypeParser<CONTEXT extends DiscordCommandContext, USER extends User> extends DiscordTypeParser<CONTEXT, USER> {
    private final Class<USER> userClass;

    public UserTypeParser(Class<USER> userClass) {
        this.userClass = userClass;
    }

    @Override
    public Mono<TypeParserResult<USER>> parse(CONTEXT context, Command command, String input) {
        var userId = Mentions.USER.parse(input)
            .or(() -> Snowflakes.parse(input));

        return userId.map(id ->
                context.guild().<User>flatMap(guild -> guild.getMemberById(id))
                    .switchIfEmpty(Mono.defer(() -> context.client().getUserById(id)))
            )
            .orElseGet(() ->
                context.guild().flatMapMany(Guild::getMembers)
                    .filter(member -> member.getDisplayName().equalsIgnoreCase(input) || member.getUsername().equalsIgnoreCase(input))
                    .cast(User.class)
                    .next()
            )
            .ofType(userClass)
            .map(this::success)
            .switchIfEmpty(failure("Failed to find a user matching %s, try mentioning them", input).mono());
    }
}
