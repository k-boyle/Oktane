package kboyle.oktane.core.module;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.preconditions.PreconditionResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Represents a command module.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Module {
    private final String name;
    private final ImmutableSet<String> groups;
    private final ImmutableList<Command> commands;
    private final ImmutableList<Precondition> preconditions;
    private final Optional<String> description;
    private final ImmutableList<Class<?>> beans;
    private final boolean singleton;
    private final boolean synchronised;

    private Module(
            String name,
            ImmutableSet<String> groups,
            List<Command.Builder> commands,
            ImmutableList<Precondition> preconditions,
            Optional<String> description,
            ImmutableList<Class<?>> beans,
            boolean singleton,
            boolean synchronised) {
        this.name = name;
        this.groups = groups;
        this.commands = commands.stream()
            .map(command -> command.build(this))
            .sorted(Comparator.comparingInt(Command::priority).reversed())
            .collect(ImmutableList.toImmutableList());
        this.preconditions = preconditions;
        this.description = description;
        this.beans = beans;
        this.singleton = singleton;
        this.synchronised = synchronised;
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * Runs the preconditions that belong to this module.
     * @param context The context to pass to the preconditions.
     * @return The result of executing the preconditions.
     */
    public PreconditionResult runPreconditions(CommandContext context) {
        return CommandUtil.runPreconditions(context, preconditions);
    }

    /**
     * @return The module's name.
     */
    public String name() {
        return name;
    }

    /**
     * @return The module's groups.
     */
    public ImmutableSet<String> groups() {
        return groups;
    }

    /**
     * @return The module's commands.
     */
    public ImmutableList<Command> commands() {
        return commands;
    }

    /**
     * @return The module's description.
     */
    public Optional<String> description() {
        return description;
    }

    /**
     * @return The types of the beans that this module requires.
     */
    public ImmutableList<Class<?>> beans() {
        return beans;
    }

    /**
     * @return Whether the module is a singleton or not.
     */
    public boolean singleton() {
        return singleton;
    }

    /**
     * @return Whether the all of the commands that belong to this module are synchronised.
     */
    public boolean synchronised() {
        return synchronised;
    }

    static class Builder {
        private static final String SPACE = " ";

        private final ImmutableSet.Builder<String> groups;
        private final List<Command.Builder> commands;
        private final ImmutableList.Builder<Precondition> preconditions;
        private final ImmutableList.Builder<Class<?>> beans;

        private String name;
        private String description;
        private boolean singleton;
        private boolean synchronised;

        private Builder() {
            this.groups = ImmutableSet.builder();
            this.commands = new ArrayList<>();
            this.preconditions = ImmutableList.builder();
            this.beans = ImmutableList.builder();
        }

        public Builder withName(String name) {
            Preconditions.checkNotNull(name, "Name cannot be null");
            this.name = name;
            return this;
        }

        public Builder withGroup(String group) {
            Preconditions.checkNotNull(group, "Group cannot be null");
            Preconditions.checkState(!group.contains(SPACE), "Group %s contains a space", group);
            this.groups.add(group);
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withCommand(Command.Builder command) {
            Preconditions.checkNotNull(command, "command cannot be null");
            this.commands.add(command);
            return this;
        }

        public Builder withPrecondition(Precondition precondition) {
            Preconditions.checkNotNull(precondition, "precondition cannot be null");
            this.preconditions.add(precondition);
            return this;
        }

        public Builder withBean(Class<?> bean) {
            Preconditions.checkNotNull(bean, "bean cannot be null");
            this.beans.add(bean);
            return this;
        }

        public Builder singleton() {
            this.singleton = true;
            return this;
        }

        public Builder withSingleton(boolean singleton) {
            this.singleton = singleton;
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

        public Module build() {
            Preconditions.checkNotNull(name, "A module name must be specified");

            return new Module(
                name,
                groups.build(),
                commands,
                preconditions.build(),
                Optional.ofNullable(description),
                beans.build(),
                singleton,
                synchronised);
        }
    }
}
