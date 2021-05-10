package kboyle.oktane.example.modules;

import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.module.annotations.Optional;
import kboyle.oktane.core.module.annotations.Remainder;
import kboyle.oktane.core.results.command.CommandResult;

public class OptionalModule extends ExampleModuleBase {
    @Aliases("optional1")
    public CommandResult optional1(@Optional @Remainder String input) {
        return message(input);
    }

    @Aliases("optional2")
    public CommandResult optional2(@Optional(defaultValue = "pong") String input) {
        return message(input);
    }

    @Aliases("optional3")
    public CommandResult optional3(@Optional String a, @Optional(defaultValue = "this is the default") String b) {
        return message("a: " + a + ", b: " + b);
    }

    @Aliases("optional4")
    public CommandResult optional4(@Optional int x) {
        return message("x: " + x);
    }
}
