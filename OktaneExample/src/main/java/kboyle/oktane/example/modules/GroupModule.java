package kboyle.oktane.example.modules;

import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.example.ExampleCommandContext;

@Aliases("group")
public class GroupModule extends ModuleBase<ExampleCommandContext> {
    @Aliases({"a1", "a2"})
    public CommandResult a() {
        return nop();
    }

    public CommandResult b() {
        return message("This is a group command");
    }
}
