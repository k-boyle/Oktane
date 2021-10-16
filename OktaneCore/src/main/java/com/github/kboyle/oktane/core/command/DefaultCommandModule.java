package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.Utilities.Functions;
import com.github.kboyle.oktane.core.execution.ModuleBase;
import com.github.kboyle.oktane.core.precondition.Precondition;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.*;

import static com.github.kboyle.oktane.core.Utilities.Objects.getIfNull;

public class DefaultCommandModule extends AbstractCommandComponent implements CommandModule {
    private final List<Precondition> preconditions;
    private final List<Command> commands;
    private final List<Class<?>> dependencies;
    private final List<CommandModule> children;
    private final Set<String> groups;
    private final CommandModule parent;
    private final Class<? extends ModuleBase<?>> originalClass;
    private final boolean singleton;
    private final boolean synchronised;
    private final Runnable before;
    private final Runnable after;

    protected DefaultCommandModule(Builder builder) {
        super(builder);

        this.preconditions = List.copyOf(builder.preconditions());
        this.commands = builder.commands().stream()
            .map(command -> command.build(this))
            .sorted(Comparator.reverseOrder())
            .toList();
        this.dependencies = List.copyOf(builder.dependencies());
        this.children = builder.children().stream()
            .map(child -> child.build(this))
            .toList();
        this.groups = Set.copyOf(builder.groups());
        this.parent = builder.parent();
        this.originalClass = builder.originalClass();
        this.singleton = builder.singleton();
        this.synchronised = builder.synchronised();
        this.before = getIfNull(builder.before(), Functions::runNothing);
        this.after = getIfNull(builder.after(), Functions::runNothing);
    }

    @Override
    public List<Precondition> preconditions() {
        return preconditions;
    }

    @Override
    public List<Command> commands() {
        return commands;
    }

    @Override
    public List<Class<?>> dependencies() {
        return dependencies;
    }

    @Override
    public List<CommandModule> children() {
        return children;
    }

    @Override
    public Set<String> groups() {
        return groups;
    }

    @Override
    public Optional<CommandModule> parent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Optional<Class<? extends ModuleBase<?>>> originalClass() {
        return Optional.ofNullable(originalClass);
    }

    @Override
    public boolean singleton() {
        return singleton;
    }

    @Override
    public boolean synchronised() {
        return synchronised;
    }

    @Override
    public Runnable before() {
        return before;
    }

    @Override
    public Runnable after() {
        return after;
    }

    protected static class Builder extends AbstractCommandComponent.Builder<Builder> implements CommandModule.Builder {
        protected final List<Precondition> preconditions;
        protected final List<Command.Builder> commands;
        protected final Set<String> groups;
        protected final List<Class<?>> dependencies;
        protected final List<CommandModule.Builder> children;

        protected CommandModule parent;
        protected Class<? extends ModuleBase<?>> originalClass;
        protected boolean singleton;
        protected boolean synchronised;
        protected Runnable before;
        private Runnable after;

        protected Builder() {
            this.preconditions = new ArrayList<>();
            this.commands = new ArrayList<>();
            this.groups = new HashSet<>();
            this.dependencies = new ArrayList<>();
            this.children = new ArrayList<>();
        }

        @Override
        public CommandModule.Builder precondition(Precondition precondition) {
            preconditions.add(Preconditions.checkNotNull(precondition, "precondition cannot be null"));
            return this;
        }

        @Override
        public CommandModule.Builder command(Command.Builder command) {
            commands.add(Preconditions.checkNotNull(command, "command cannot be null"));
            return this;
        }

        @Override
        public CommandModule.Builder dependency(Class<?> dependency) {
            dependencies.add(Preconditions.checkNotNull(dependency, "dependency cannot be null"));
            return this;
        }

        @Override
        public CommandModule.Builder child(CommandModule.Builder child) {
            children.add(Preconditions.checkNotNull(child, "child cannot be null"));
            return this;
        }

        @Override
        public CommandModule.Builder group(String group) {
            Preconditions.checkState(!Strings.isNullOrEmpty(group), "group must not be null or empty");
            groups.add(group);
            return this;
        }

        @Override
        public CommandModule.Builder parent(CommandModule parent) {
            this.parent = Preconditions.checkNotNull(parent, "parent cannot be null");
            return this;
        }

        @Override
        public CommandModule.Builder originalClass(Class<? extends ModuleBase<?>> originalClass) {
            this.originalClass = Preconditions.checkNotNull(originalClass, "originalClass cannot be null");
            return this;
        }

        @Override
        public CommandModule.Builder singleton(boolean singleton) {
            this.singleton = singleton;
            return this;
        }

        @Override
        public CommandModule.Builder synchronised(boolean synchronised) {
            this.synchronised = synchronised;
            return this;
        }

        @Override
        public CommandModule.Builder before(Runnable before) {
            this.before = Preconditions.checkNotNull(before, "before cannot be null");
            return this;
        }

        @Override
        public CommandModule.Builder after(Runnable after) {
            this.after = Preconditions.checkNotNull(after, "after cannot be null");
            return this;
        }

        @Override
        public List<Precondition> preconditions() {
            return preconditions;
        }

        @Override
        public List<Command.Builder> commands() {
            return commands;
        }

        @Override
        public List<Class<?>> dependencies() {
            return dependencies;
        }

        @Override
        public List<CommandModule.Builder> children() {
            return children;
        }

        @Override
        public Set<String> groups() {
            return groups;
        }

        @Override
        public CommandModule parent() {
            return parent;
        }

        @Override
        public Class<? extends ModuleBase<?>> originalClass() {
            return originalClass;
        }

        @Override
        public boolean singleton() {
            return singleton;
        }

        @Override
        public boolean synchronised() {
            return synchronised;
        }

        @Override
        public Runnable before() {
            return before;
        }

        @Override
        public Runnable after() {
            return after;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public CommandModule build(CommandModule parent) {
            this.parent = parent;
            return new DefaultCommandModule(this);
        }
    }
}
