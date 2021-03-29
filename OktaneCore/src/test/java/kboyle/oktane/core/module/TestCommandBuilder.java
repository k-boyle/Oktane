package kboyle.oktane.core.module;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestCommandBuilder {
    private final List<CommandParameter.Builder> parameters = new ArrayList<>();

    public TestCommandBuilder addParameter(Class<?> type, boolean remainder) {
        this.parameters.add(CommandParameter.builder().withName("").withType(type).withRemainder(remainder));
        return this;
    }

    public Command build() {
        return new Command(
            "",
            ImmutableSet.of(),
            Optional.empty(),
            null,
            parameters,
            ImmutableList.of(),
            null,
            false,
            0);
    }
}
