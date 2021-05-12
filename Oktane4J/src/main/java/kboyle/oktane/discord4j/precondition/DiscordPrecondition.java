package kboyle.oktane.discord4j.precondition;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import reactor.core.publisher.Mono;

public abstract class DiscordPrecondition<CONTEXT extends DiscordCommandContext> implements Precondition {
    @Override
    public Mono<PreconditionResult> run(CommandContext context, Command command) {
        return run((CONTEXT) context, command);
    }

    public abstract Mono<PreconditionResult> run(CONTEXT context, Command command);
}
