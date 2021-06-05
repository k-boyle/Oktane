package kboyle.oktane.discord4j.parsers;

import discord4j.core.object.entity.Role;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.Mentions;
import kboyle.oktane.discord4j.Snowflakes;
import reactor.core.publisher.Mono;

/**
 * A {@link DiscordTypeParser} for parsing {@link Role}'s.
 */
public class RoleTypeParser extends DiscordTypeParser<Role> {
    private final Mono<TypeParserResult<Role>> notInGuild = failure("Roles can only be parsed within a guild context").mono();

    @Override
    public Mono<TypeParserResult<Role>> parse(DiscordCommandContext context, Command command, String input) {
        var roleId = Mentions.ROLE.parseExact(input)
            .or(() -> Snowflakes.parse(input));

        return context.guild().flatMap(guild ->
                roleId.map(guild::getRoleById)
                    .orElseGet(() ->
                        guild.getRoles()
                            .filter(role -> role.getName().equalsIgnoreCase(input))
                            .next()
                    )
            )
            .map(this::success)
            .switchIfEmpty(notInGuild);
    }
}
