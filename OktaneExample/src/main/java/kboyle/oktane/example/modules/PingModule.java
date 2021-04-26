package kboyle.oktane.example.modules;

import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.module.annotations.Description;
import kboyle.oktane.core.module.annotations.Name;
import kboyle.oktane.core.module.annotations.Remainder;
import kboyle.oktane.core.processor.OktaneModule;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.example.ExampleEnum;

@Name("Ping Module")
@Description("A module with various... useful... ping commands")
@OktaneModule
public class PingModule extends ExampleModuleBase {
    @Name("Ping")
    @Aliases("ping")
    @Description("Replies with pong")
    public CommandResult ping() {
        return message("pong");
    }

    @Name("Ping Two")
    @Aliases("ping")
    @Description("Adds the two numbers")
    public CommandResult ping(int a, int b) {
        return message("pong: " + (a + b));
    }

    @Name("Ping Echo")
    @Aliases("echo")
    @Description("Echos the input")
    public CommandResult echo(@Remainder @Name("Echo") String echo) {
        return message("pong " + echo);
    }

    @Name("Ping Enum")
    @Aliases("ping")
    @Description("Echos the chosen enum")
    public CommandResult ping(@Name("Enum") ExampleEnum exampleEnum) {
        return message("ping " + exampleEnum);
    }
}
