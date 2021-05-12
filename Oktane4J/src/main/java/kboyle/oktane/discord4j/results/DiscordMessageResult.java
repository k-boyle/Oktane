package kboyle.oktane.discord4j.results;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.SuccessfulResult;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class DiscordMessageResult extends DiscordResult implements SuccessfulResult {
    private final Command command;
    private final MessageChannel channel;
    private final Consumer<MessageCreateSpec> messageCreateSpecConsumer;

    public DiscordMessageResult(
            Command command,
            MessageChannel channel,
            Consumer<MessageCreateSpec> messageCreateSpecConsumer) {
        this.command = command;
        this.channel = channel;
        this.messageCreateSpecConsumer = messageCreateSpecConsumer;
    }

    @Override
    public Mono<Void> execute() {
        return channel.createMessage(messageCreateSpecConsumer).then();
    }

    @Override
    public Command command() {
        return command;
    }
}
