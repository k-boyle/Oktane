package kboyle.oktane.discord4j.results;

import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.SuccessfulResult;
import reactor.core.publisher.Mono;

public class DiscordReactResult extends DiscordResult implements SuccessfulResult {
    private final Command command;
    private final Message message;
    private final ReactionEmoji reactionEmoji;

    public DiscordReactResult(Command command, Message message, ReactionEmoji reactionEmoji) {
        this.command = command;
        this.message = message;
        this.reactionEmoji = reactionEmoji;
    }

    @Override
    public Mono<Void> execute() {
        return message.addReaction(reactionEmoji).then();
    }

    @Override
    public Command command() {
        return command;
    }
}
