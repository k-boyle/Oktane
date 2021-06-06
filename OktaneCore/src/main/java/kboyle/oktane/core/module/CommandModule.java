package kboyle.oktane.core.module;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.CommandUtils;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * Represents a command module.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class CommandModule {
    public final String name;
    public final ImmutableSet<String> groups;
    public final ImmutableList<Command> commands;
    public final ImmutableList<Precondition> preconditions;
    public final Optional<String> description;
    public final ImmutableList<Class<?>> beans;
    public final boolean singleton;
    public final boolean synchronised;
    public final Optional<CommandModule> parent;
    public final ImmutableList<CommandModule> children;
    public final Optional<Class<? extends ModuleBase<?>>> originalClass;

    private CommandModule(CommandModule parent, Builder builder) {
        Preconditions.checkState(!Strings.isNullOrEmpty(builder.name), "A module name must be a non-empty value");

        this.name = builder.name;
        this.groups = ImmutableSet.copyOf(builder.groups);
        this.commands = builder.commands.stream()
            .map(command -> command.build(this))
            .sorted(Comparator.comparingInt((ToIntFunction<Command>) command -> command.priority).reversed()) //lol
            .collect(ImmutableList.toImmutableList());
        this.preconditions = ImmutableList.copyOf(builder.preconditions);
        this.description = Optional.ofNullable(builder.description);
        this.beans = ImmutableList.copyOf(builder.beans);
        this.singleton = builder.singleton;
        this.synchronised = builder.synchronised;
        this.parent = Optional.ofNullable(parent);
        this.children = builder.children.stream()
            .map(child -> child.build(this))
            .collect(ImmutableList.toImmutableList());
        this.originalClass = Optional.ofNullable(builder.originalClass);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Runs the preconditions that belong to this module.
     * @param context The context to pass to the preconditions.
     * @return The result of executing the preconditions.
     */
    public Mono<PreconditionResult> runPreconditions(CommandContext context, Command command) {
        return parent.map(parent ->
            parent.runPreconditions(context, command)
                .flatMap(result -> {
                    if (!result.success()) {
                        return Mono.just(result);
                    }

                    return CommandUtils.runPreconditions(context, command, preconditions);
                })
            )
            .orElseGet(() -> CommandUtils.runPreconditions(context, command, preconditions));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .toString();
    }

    public static class Builder {
        private static final String SPACE = " ";

        public final Set<String> groups;
        public final List<Command.Builder> commands;
        public final List<Precondition> preconditions;
        public final List<Class<?>> beans;
        public final List<Builder> children;

        private String name;
        private String description;
        private boolean singleton;
        private boolean synchronised;
        private Class<? extends ModuleBase<?>> originalClass;

        private Builder() {
            this.groups = new HashSet<>();
            this.commands = new ArrayList<>();
            this.preconditions = new ArrayList<>();
            this.beans = new ArrayList<>();
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

        public Builder withCommand(Command.Builder command) {
            Preconditions.checkNotNull(command, "command cannot be null");
            this.commands.add(command);
            return this;
        }

        public Builder withCommand(Consumer<Command.Builder> builderConsumer) {
            Preconditions.checkNotNull(builderConsumer, "builderConsumer cannot be null");
            var builder = Command.builder();
            builderConsumer.accept(builder);
            this.commands.add(builder);
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

        public Builder withChild(Builder child) {
            this.children.add(child);
            return this;
        }

        public Builder withOriginalClass(Class<? extends ModuleBase<?>> originalClass) {
            this.originalClass = originalClass;
            return this;
        }

        public CommandModule build() {
            return build(null);
        }

        CommandModule build(CommandModule parent) {
            return new CommandModule(parent, this);
        }
    }
}
