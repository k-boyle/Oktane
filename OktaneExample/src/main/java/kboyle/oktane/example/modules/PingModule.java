package kboyle.oktane.example.modules;

import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.module.annotations.*;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.example.ExampleCommandContext;

@Name("Ping Module")
@Description("A module with various... useful... ping commands")
public class PingModule extends CommandModuleBase<ExampleCommandContext> {
    @Name("Ping")
    @Aliases("ping")
    @Description("Replies with pong")
    public CommandResult ping() {
        return message("pong");
    }

    @Name("Ping Two")
    @Aliases("ping")
    @Description("Adds the two numbers")
    @Disabled(reason = "Currently there's an issue with overload handling causing this command to not be executed")
    public CommandResult ping(int a, int b) {
        return message("pong: " + a + b);
    }

    @Name("Ping Echo")
    @Aliases("ping")
    @Description("Echos the input")
    public CommandResult ping(
        @Remainder
        @Name("Echo")
            String echo) {
        return message("pong " + echo);
    }
}
