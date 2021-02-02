package kb.octane.core.module;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import kb.octane.core.CommandContext;
import kb.octane.core.results.FailedResult;
import kb.octane.core.results.precondition.PreconditionResult;
import kb.octane.core.results.precondition.PreconditionsFailedResult;
import kb.octane.core.results.precondition.SuccessfulPreconditionResult;

import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Command {
    private final String name;
    private final ImmutableSet<String> aliases;
    private final Optional<String> description;
    private final CommandCallback commandCallback;
    private final ImmutableList<CommandParameter> parameters;
    private final ImmutableList<Precondition> preconditions;
    private final Signature signature;
    private final Module module;
    private final boolean synchronised;

    public Command(
            String name,
            ImmutableSet<String> aliases,
            Optional<String> description,
            CommandCallback commandCallback,
            ImmutableList<CommandParameter> parameters,
            ImmutableList<Precondition> preconditions,
            Signature signature,
            Module module,
            boolean synchronised) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
        this.commandCallback = commandCallback;
        this.parameters = parameters;
        this.preconditions = preconditions;
        this.signature = signature;
        this.module = module;
        this.synchronised = synchronised;
    }

    public static Builder builder() {
        return new Builder();
    }

    public PreconditionResult runPreconditions(CommandContext context) {
        PreconditionResult moduleResult = module.runPreconditions(context);

        if (!moduleResult.isSuccess()) {
            return moduleResult;
        }

        if (preconditions.isEmpty()) {
            return SuccessfulPreconditionResult.get();
        }

        ImmutableList.Builder<FailedResult> failedResults = ImmutableList.builder();
        boolean failedResult = false;

        for (Precondition precondition : preconditions) {
            PreconditionResult result = precondition.run(context);
            if (result instanceof FailedResult failed) {
                failedResults.add(failed);
                failedResult = true;
            }
        }

        return failedResult
            ? new PreconditionsFailedResult(failedResults.build())
            : SuccessfulPreconditionResult.get();
    }

    public String name() {
        return name;
    }

    public ImmutableSet<String> aliases() {
        return aliases;
    }

    public Optional<String> description() {
        return description;
    }

    public CommandCallback commandCallback() {
        return commandCallback;
    }

    public ImmutableList<CommandParameter> parameters() {
        return parameters;
    }

    public ImmutableList<Precondition> preconditions() {
        return preconditions;
    }

    public Signature signature() {
        return signature;
    }

    public Module module() {
        return module;
    }

    public boolean synchronised() {
        return synchronised;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .toString();
    }

    public static class Builder {
        private static final String SPACE = " ";

        private final ImmutableSet.Builder<String> aliases;
        private final ImmutableList.Builder<CommandParameter> parameters;
        private final ImmutableList.Builder<Precondition> preconditions;

        private String name;
        private String description;
        private CommandCallback commandCallback;
        private boolean synchronised;

        private Builder() {
            this.aliases = ImmutableSet.builder();
            this.parameters = ImmutableList.builder();
            this.preconditions = ImmutableList.builder();
        }

        public Builder withName(String name) {
            Preconditions.checkNotNull(name, "name cannot be null");
            this.name = name;
            return this;
        }

        public Builder withAliases(String alias) {
            Preconditions.checkNotNull(alias, "alias cannot be null");
            Preconditions.checkState(!alias.contains(SPACE), "Alias %s contains a space", alias);
            this.aliases.add(alias);
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withCallback(CommandCallback commandCallback) {
            Preconditions.checkNotNull(commandCallback, "commandCallback cannot be null");
            this.commandCallback = commandCallback;
            return this;
        }

        public Builder withParameter(CommandParameter commandParameter) {
            Preconditions.checkNotNull(commandParameter, "commandParameter cannot be null");
            this.parameters.add(commandParameter);
            return this;
        }

        public Builder withPrecondition(Precondition precondition) {
            Preconditions.checkNotNull(precondition, "precondition cannot be null");
            this.preconditions.add(precondition);
            return this;
        }

        public Builder synchronised() {
            this.synchronised = true;
            return this;
        }

        public Builder withSynchronised(boolean synchronised) {
            this.synchronised = synchronised;
            return this;
        }

        Command build(Module module) {
            Preconditions.checkNotNull(name, "A command name must be specified");
            Preconditions.checkNotNull(commandCallback, "A command callback must be specified");

            ImmutableSet<String> aliases = this.aliases.build();
            Preconditions.checkState(
                isValidAliases(aliases, module.groups()),
                "A command must have a non-empty alias if there are no module groups"
            );

            ImmutableList<CommandParameter> parameters = this.parameters.build();
            for (int i = 0; i < parameters.size(); i++) {
                CommandParameter commandParameter = parameters.get(i);
                Preconditions.checkState(
                    !commandParameter.remainder() || i == parameters.size() - 1,
                    "Parameter %s of Command %s cannot be remainder only the final parameter can be remainder",
                    commandParameter.name(),
                    name
                );
            }

            Signature commandSignature = new Signature(
                !parameters.isEmpty() && parameters.get(parameters.size() - 1).remainder(),
                parameters.stream()
                    .map(CommandParameter::type)
                    .map(Class::toString)
                    .collect(Collectors.joining(";"))
            );

            return new Command(
                name,
                aliases,
                Optional.ofNullable(description),
                commandCallback,
                parameters,
                preconditions.build(),
                commandSignature,
                module,
                synchronised);
        }

        private static boolean isValidAliases(ImmutableSet<String> commandAliases, ImmutableSet<String> moduleGroups) {
            return commandAliases.size() > 0 || moduleGroups.size() > 0;
        }
    }

    public static record Signature(boolean remainder, String parameters) {
    }
}
