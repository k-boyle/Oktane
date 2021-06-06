package kboyle.oktane.core.module;

import kboyle.oktane.core.results.command.CommandNOPResult;

import java.util.ArrayList;
import java.util.List;

public class TestCommandBuilder {
    private static int counter;
    private final List<CommandParameter.Builder> parameters = new ArrayList<>();

    public TestCommandBuilder addParameter(Class<?> type, boolean remainder) {
        this.parameters.add(CommandParameter.builder().withName(String.valueOf(counter++)).withType(type).withRemainder(remainder));
        return this;
    }

    public TestCommandBuilder addOptionalParameter(Class<?> type, String defaultValue) {
        this.parameters.add(CommandParameter.builder().withName(String.valueOf(counter++)).withType(type).withDefaultValue(defaultValue));
        return this;
    }

    public Command build() {
        var dummyModule = CommandModule.builder()
            .withName(String.valueOf(counter++))
            .withGroup("")
            .build();

        var commandBuilder = Command.builder()
            .withName(String.valueOf(counter++))
            .withCallback((ctx, beans, parameters) -> new CommandNOPResult(ctx.command()).mono());
        parameters.forEach(commandBuilder::withParameter);

        return new Command(dummyModule, commandBuilder);
    }
}
