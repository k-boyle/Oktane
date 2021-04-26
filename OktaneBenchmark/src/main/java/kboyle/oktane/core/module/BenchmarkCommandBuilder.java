package kboyle.oktane.core.module;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return new Command(
            "",
            ImmutableSet.of(),
            Optional.empty(),
            null,
            parameters,
            ImmutableList.of(),
            null,
            false,
            0
        );
    }
}
