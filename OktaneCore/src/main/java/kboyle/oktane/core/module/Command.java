package kboyle.oktane.core.module;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.FailedResult;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.precondition.PreconditionsFailedResult;
import kboyle.oktane.core.results.precondition.SuccessfulPreconditionResult;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a command.
 */
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

    Command(
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

    static Builder builder() {
        return new Builder();
    }

    /**
     * Runs the preconditions that belong to this command.
     * @param context The context to pass to the preconditions.
     * @return The result of executing the preconditions.
     */
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

    /**
     * @return The command's name.
     */
    public String name() {
        return name;
    }

    /**
     * @return The command's aliases.
     */
    public ImmutableSet<String> aliases() {
        return aliases;
    }

    /**
     * @return The command's description.
     */
    public Optional<String> description() {
        return description;
    }

    /**
     * @return The method of the command.
     */
    public CommandCallback commandCallback() {
        return commandCallback;
    }

    /**
     * @return The command's parameters.
     */
    public ImmutableList<CommandParameter> parameters() {
        return parameters;
    }

    /**
     * @return The command's preconditions.
     */
    public ImmutableList<Precondition> preconditions() {
        return preconditions;
    }

    /**
     * @return The command's signature that's used to determine uniqueness.
     */
    public Signature signature() {
        return signature;
    }

    /**
     * @return The Module that this command belongs to.
     */
    public Module module() {
        return module;
    }

    /**
     * @return Whether the execution of the command is synchronised or not.
     */
    public boolean synchronised() {
        return synchronised;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .toString();
    }

    static class Builder {
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

            ImmutableSet<String> builtAliases = this.aliases.build();
            Preconditions.checkState(
                isValidAliases(builtAliases, module.groups()),
                "A command must have a non-empty alias if there are no module groups"
            );

            ImmutableList<CommandParameter> builtParameters = this.parameters.build();
            for (int i = 0; i < builtParameters.size(); i++) {
                CommandParameter commandParameter = builtParameters.get(i);
                Preconditions.checkState(
                    !commandParameter.remainder() || i == builtParameters.size() - 1,
                    "Parameter %s of Command %s cannot be remainder only the final parameter can be remainder",
                    commandParameter.name(),
                    name
                );
            }

            Signature commandSignature = new Signature(
                !builtParameters.isEmpty() && builtParameters.get(builtParameters.size() - 1).remainder(),
                builtParameters.stream()
                    .map(CommandParameter::type)
                    .map(Class::toString)
                    .collect(Collectors.joining(";"))
            );

            return new Command(
                name,
                builtAliases,
                Optional.ofNullable(description),
                commandCallback,
                builtParameters,
                preconditions.build(),
                commandSignature,
                module,
                synchronised);
        }

        private static boolean isValidAliases(ImmutableSet<String> commandAliases, ImmutableSet<String> moduleGroups) {
            return !commandAliases.isEmpty() || !moduleGroups.isEmpty();
        }
    }

    public static record Signature(boolean remainder, String parameters) {
    }
}
