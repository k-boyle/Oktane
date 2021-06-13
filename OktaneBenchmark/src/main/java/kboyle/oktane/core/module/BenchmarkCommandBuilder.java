package kboyle.oktane.core.module;

import kboyle.oktane.core.results.command.CommandNOPResult;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkCommandBuilder {
    private static int counter;

    private final List<CommandParameter.Builder> parameters;

    public BenchmarkCommandBuilder() {
        parameters = new ArrayList<>();
    }

    public BenchmarkCommandBuilder withParameter() {
        parameters.add(CommandParameter.builder().withType(String.class).withName(String.valueOf(counter++)));
        return this;
    }

    public BenchmarkCommandBuilder withRemainderParameter() {
        parameters.add(CommandParameter.builder().withType(String.class).withName(String.valueOf(counter++)).withRemainder(true));
        return this;
    }

    public Command create() {
        var dummyModule = CommandModule.builder()
            .withName(String.valueOf(counter++))
            .withGroup("")
            .build();

        var builder = Command.builder()
            .withName(String.valueOf(counter++))
            .withCallback((ctx, beans, parameters) -> new CommandNOPResult(ctx.command()).mono());
        parameters.forEach(builder::withParameter);

        return new Command(dummyModule, builder);
    }
}
