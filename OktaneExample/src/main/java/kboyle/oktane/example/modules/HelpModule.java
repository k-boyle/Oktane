package kboyle.oktane.example.modules;

import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.processor.OktaneModule;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.example.ExampleCommandContext;

import java.util.stream.Collectors;

@OktaneModule
public class HelpModule extends ModuleBase<ExampleCommandContext> {
    private final CommandHandler<ExampleCommandContext> commandHandler;

    public HelpModule(CommandHandler<ExampleCommandContext> commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Aliases("help")
    public CommandResult help() {
        String helpMessage = commandHandler.modules().stream()
            .map(module -> {
                String formattedCommands = module.commands.stream()
                    .flatMap(command -> command.aliases.stream())
                    .map(alias -> "- " + alias)
                    .collect(Collectors.joining("\n"));
                return module.name + "\n" + formattedCommands;
            })
            .collect(Collectors.joining("\n"));

        return message(helpMessage);
    }
}
