package kboyle.oktane.core.module;

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
        var builder = Command.builder();
        parameters.forEach(builder::withParameter);

        return new Command(null, builder);
    }
}
