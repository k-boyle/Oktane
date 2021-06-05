package kboyle.oktane.discord4j.precondition;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.exceptions.InvalidContextTypeException;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import reactor.core.publisher.Mono;

public abstract class DiscordPrecondition implements Precondition {
    @Override
    public Mono<PreconditionResult> run(CommandContext context, Command command) {
        if (context instanceof DiscordCommandContext discordCommandContext) {
            return run(discordCommandContext, command);
        }

        throw new InvalidContextTypeException(DiscordCommandContext.class, context.getClass());
    }

    public abstract Mono<PreconditionResult> run(DiscordCommandContext context, Command command);
}
