package kboyle.oktane.example.modules;

import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.example.ExampleCommandContext;

public class HelpModule extends CommandModuleBase<ExampleCommandContext> {
    private final CommandHandler<ExampleCommandContext> commandHandler;

    public HelpModule(CommandHandler<ExampleCommandContext> commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Aliases("help")
    public CommandResult help() {
        return nop();
    }
}
