package kboyle.oktane.reactive.module;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Represents a command module.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ReactiveModule {
    private final String name;
    private final ImmutableSet<String> groups;
    private final ImmutableList<ReactiveCommand> commands;
    private final ImmutableList<ReactivePrecondition> preconditions;
    private final Optional<String> description;
    private final ImmutableList<Class<?>> beans;
    private final boolean singleton;
    private final boolean synchronised;

    private ReactiveModule(
            String name,
            ImmutableSet<String> groups,
            List<ReactiveCommand.Builder> commands,
            ImmutableList<ReactivePrecondition> preconditions,
            Optional<String> description,
            ImmutableList<Class<?>> beans,
            boolean singleton,
            boolean synchronised) {
        this.name = name;
        this.groups = groups;
        this.commands = commands.stream()
            .map(command -> command.build(this))
            .sorted(Comparator.comparingInt(ReactiveCommand::priority).reversed())
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
    public Mono<PreconditionResult> runPreconditions(CommandContext context, ReactiveCommand command) {
        return CommandUtil.runPreconditions(context, command, preconditions);
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
    public ImmutableList<ReactiveCommand> commands() {
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
        private final List<ReactiveCommand.Builder> commands;
        private final ImmutableList.Builder<ReactivePrecondition> preconditions;
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

        public Builder withCommand(ReactiveCommand.Builder command) {
            Preconditions.checkNotNull(command, "command cannot be null");
            this.commands.add(command);
            return this;
        }

        public Builder withPrecondition(ReactivePrecondition precondition) {
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

        public ReactiveModule build() {
            Preconditions.checkNotNull(name, "A module name must be specified");

            return new ReactiveModule(
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
