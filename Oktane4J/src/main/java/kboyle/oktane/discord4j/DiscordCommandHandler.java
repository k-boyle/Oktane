package kboyle.oktane.discord4j;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.results.Result;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class DiscordCommandHandler<CONTEXT extends DiscordCommandContext> {
    private final CommandHandler<CONTEXT> commandHandler;

    private DiscordCommandHandler(CommandHandler<CONTEXT> commandHandler) {
        this.commandHandler = commandHandler;
    }

    public static <CONTEXT extends DiscordCommandContext> DiscordCommandHandler<CONTEXT> create(Consumer<CommandHandler.Builder<CONTEXT>> commandHandlerConsumer) {
        var builder = CommandHandler.<CONTEXT>builder();
        commandHandlerConsumer.accept(builder);
        return new DiscordCommandHandler<>(builder.build());
    }

    public CommandHandler<CONTEXT> innerHandler() {
        return this.commandHandler;
    }

    public Mono<Result> execute(String input, CONTEXT context) {
        return commandHandler.execute(input, context);
    }

    public Mono<Result> execute(String input, CONTEXT context, int startIndex) {
        return commandHandler.execute(input, context, startIndex);
    }

    public ImmutableList<CommandModule> modules() {
        return commandHandler.modules();
    }

    public Stream<CommandModule> flattenModules() {
        return commandHandler.flattenModules();
    }

    public Stream<Command> commands() {
        return commandHandler.commands();
    }
}
