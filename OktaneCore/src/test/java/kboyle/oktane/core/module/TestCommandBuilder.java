package kboyle.oktane.core.module;

import java.util.ArrayList;
import java.util.List;

public class TestCommandBuilder {
    private final List<CommandParameter.Builder> parameters = new ArrayList<>();

    public TestCommandBuilder addParameter(Class<?> type, boolean remainder) {
        this.parameters.add(CommandParameter.builder().withName("").withType(type).withRemainder(remainder));
        return this;
    }

    public TestCommandBuilder addOptionalParameter(Class<?> type, String defaultValue) {
        this.parameters.add(CommandParameter.builder().withName("").withType(type).withDefaultValue(defaultValue));
        return this;
    }

    public Command build() {
        var builder = Command.builder();
        parameters.forEach(builder::withParameter);

        return new Command(null, builder);
    }
}
