package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.command.CommandModule;
import com.github.kboyle.oktane.core.mapping.CommandMap;
import com.github.kboyle.oktane.core.parsing.*;
import com.github.kboyle.oktane.core.prefix.PrefixSupplier;
import com.github.kboyle.oktane.core.result.Result;
import com.google.common.base.Preconditions;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

// todo nullable annotations
public interface CommandService {
    Result execute(CommandContext context, String input, int startIndex);
    List<CommandModule> modules();
    PrefixSupplier prefixSupplier();
    CommandMap commandMap();
    Tokeniser tokeniser();
    ArgumentParser argumentParser();
    TypeParserProvider typeParserProvider();

    default Result execute(CommandContext context, String input) {
        return execute(context, input, 0);
    }

    default Result execute(CommandContext context, Command command, Object[] arguments) {
        Preconditions.checkNotNull(context, "context cannot be null");
        Preconditions.checkNotNull(command, "command cannot be null");
        Preconditions.checkNotNull(arguments, "arguments cannot be null");
        Preconditions.checkState(
            command.parameters().size() == arguments.length,
            "Expected %d arguments but got %d",
            command.parameters().stream(),
            arguments.length
        );

        context.command = command;
        var preconditionResult = command.runPreconditions(context);
        if (!preconditionResult.success()) {
            return preconditionResult;
        }

        context.arguments = arguments;
        return command.callback().execute(context);
    }

    default Stream<Command> commands() {
        var modules = Preconditions.checkNotNull(modules(), "CommandService#modules cannot return null");
        return modules.stream()
            .mapMulti(CommandService::flattenCommands);
    }

    default Stream<CommandModule> allModules() {
        var modules = Preconditions.checkNotNull(modules(), "CommandService#modules cannot return null");
        return modules.stream()
            .mapMulti(CommandService::flattenModules);
    }

    private static void flattenCommands(CommandModule module, Consumer<Command> commandConsumer) {
        Preconditions.checkNotNull(module, "module cannot be null");

        var commands = Preconditions.checkNotNull(module.commands(), "CommandModule#commands cannot return null");
        for (var command : commands) {
            Preconditions.checkNotNull(command, "command cannot be null");
            commandConsumer.accept(command);
        }

        for (var child : module.children()) {
            flattenCommands(child, commandConsumer);
        }
    }

    private static void flattenModules(CommandModule module, Consumer<CommandModule> commandModuleConsumer) {
        Preconditions.checkNotNull(module, "module cannot be null");

        commandModuleConsumer.accept(module);

        for (var child : module.children()) {
            Preconditions.checkNotNull(child, "child cannot be null");
            flattenModules(child, commandModuleConsumer);
        }
    }
}
