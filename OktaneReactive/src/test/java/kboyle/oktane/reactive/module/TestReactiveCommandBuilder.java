package kboyle.oktane.reactive.module;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestReactiveCommandBuilder {
    private final List<ReactiveCommandParameter.Builder> parameters = new ArrayList<>();

    public TestReactiveCommandBuilder addParameter(Class<?> type, boolean remainder) {
        this.parameters.add(ReactiveCommandParameter.builder().withName("").withType(type).withRemainder(remainder));
        return this;
    }

    public ReactiveCommand build() {
        return new ReactiveCommand(
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
