package kboyle.oktane.discord4j.results;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.SuccessfulResult;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class DiscordMessageResult implements DiscordResult, SuccessfulResult {
    private final Command command;
    private final Mono<MessageChannel> channel;
    private final Consumer<MessageCreateSpec> messageCreateSpecConsumer;

    public DiscordMessageResult(
            Command command,
            Mono<MessageChannel> channel,
            Consumer<MessageCreateSpec> messageCreateSpecConsumer) {
        this.command = command;
        this.channel = channel;
        this.messageCreateSpecConsumer = messageCreateSpecConsumer;
    }

    @Override
    public Mono<Void> execute() {
        return channel.flatMap(channel -> channel.createMessage(messageCreateSpecConsumer)).then();
    }

    @Override
    public Command command() {
        return command;
    }
}
