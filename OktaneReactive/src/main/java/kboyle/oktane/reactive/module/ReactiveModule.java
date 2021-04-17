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
import java.util.function.ToIntFunction;

/**
 * Represents a command module.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ReactiveModule {
    public final String name;
    public final ImmutableSet<String> groups;
    public final ImmutableList<ReactiveCommand> commands;
    public final ImmutableList<ReactivePrecondition> preconditions;
    public final Optional<String> description;
    public final ImmutableList<Class<?>> beans;
    public final boolean singleton;
    public final boolean synchronised;
    public final Optional<ReactiveModule> parent;
    public final ImmutableList<ReactiveModule> children;

    private ReactiveModule(
            String name,
            ImmutableSet<String> groups,
            List<ReactiveCommand.Builder> commands,
            ImmutableList<ReactivePrecondition> preconditions,
            Optional<String> description,
            ImmutableList<Class<?>> beans,
            boolean singleton,
            boolean synchronised,
            Optional<ReactiveModule> parent,
            List<ReactiveModule.Builder> children) {
        this.name = name;
        this.groups = groups;
        this.commands = commands.stream()
            .map(command -> command.build(this))
            .sorted(Comparator.comparingInt((ToIntFunction<ReactiveCommand>) command -> command.priority).reversed()) //lol
            .collect(ImmutableList.toImmutableList());
        this.preconditions = preconditions;
        this.description = description;
        this.beans = beans;
        this.singleton = singleton;
        this.synchronised = synchronised;
        this.parent = parent;
        this.children = children.stream()
            .map(child -> child.build(this))
            .collect(ImmutableList.toImmutableList());
    }

    public static Builder builder() {
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

    public static class Builder {
        private static final String SPACE = " ";

        private final ImmutableSet.Builder<String> groups;
        private final List<ReactiveCommand.Builder> commands;
        private final ImmutableList.Builder<ReactivePrecondition> preconditions;
        private final ImmutableList.Builder<Class<?>> beans;
        private final List<Builder> children;

        private String name;
        private String description;
        private boolean singleton;
        private boolean synchronised;

        private Builder() {
            this.groups = ImmutableSet.builder();
            this.commands = new ArrayList<>();
            this.preconditions = ImmutableList.builder();
            this.beans = ImmutableList.builder();
            this.children = new ArrayList<>();
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

        public Builder withChild(Builder child) {
            this.children.add(child);
            return this;
        }

        public ReactiveModule build() {
            return build(null);
        }

        public ReactiveModule build(ReactiveModule parent) {
            Preconditions.checkNotNull(name, "A module name must be specified");

            return new ReactiveModule(
                name,
                groups.build(),
                commands,
                preconditions.build(),
                Optional.ofNullable(description),
                beans.build(),
                singleton,
                synchronised,
                Optional.ofNullable(parent),
                children
            );
        }
    }
}
