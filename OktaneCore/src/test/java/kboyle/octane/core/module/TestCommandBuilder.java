package kboyle.octane.core.module;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestCommandBuilder {
    private final List<CommandParameter> parameters = new ArrayList<>();

    public TestCommandBuilder addParameter(Class<?> type, boolean remainder) {
        this.parameters.add(new CommandParameter(type, Optional.empty(), "", remainder));
        return this;
    }

    public Command build() {
        return new Command(
            "",
            ImmutableSet.of(),
            Optional.empty(),
            null,
            ImmutableList.copyOf(parameters),
            ImmutableList.of(),
            null,
            null,
            false
        );
    }
}
