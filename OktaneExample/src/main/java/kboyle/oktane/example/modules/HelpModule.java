package kboyle.oktane.example.modules;

import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.example.ExampleCommandContext;

import java.util.stream.Collectors;

public class HelpModule extends ModuleBase<ExampleCommandContext> {
    private final CommandHandler commandHandler;

    public HelpModule(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Aliases("help")
    public CommandResult help() {
        var helpMessage = commandHandler.modules().stream()
            .map(module -> {
                var formattedCommands = module.commands.stream()
                    .flatMap(command -> command.aliases.stream())
                    .map(alias -> "- " + alias)
                    .collect(Collectors.joining("\n"));
                return module.name + "\n" + formattedCommands;
            })
            .collect(Collectors.joining("\n"));

        return message(helpMessage);
    }
}
