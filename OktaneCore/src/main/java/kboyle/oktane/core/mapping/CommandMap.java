package kboyle.oktane.core.mapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;

import java.util.ArrayList;
import java.util.List;

// based on https://github.com/Quahu/Qmmands/blob/master/src/Qmmands/Mapping/CommandMap.cs
public class CommandMap {
    private final CommandMapNode rootNode;

    private CommandMap(CommandMapNode rootNode) {
        this.rootNode = rootNode;
    }

    public ImmutableList<CommandMatch> findCommands(String input) {
        return findCommands(input, 0);
    }

    public ImmutableList<CommandMatch> findCommands(String input, int startIndex) {
        return rootNode.findCommands(input, startIndex);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CommandMapNode.Builder rootNode;

        private boolean invalidState;

        private Builder() {
            this.rootNode = CommandMapNode.builder();
        }

        public Builder map(CommandModule module) {
            Preconditions.checkState(!invalidState, "CommandMap has been put into an invalid state");

            try {
                mapModule(module, new ArrayList<>());
            } catch (IllegalStateException e) {
                invalidState = true;
                throw e;
            }

            return this;
        }

        private void mapModule(CommandModule module, List<String> paths) {
            if (module.groups.isEmpty()) {
                mapCommands(module, paths);
                mapChildren(module, paths);
                return;
            }

            for (String group : module.groups) {
                if (group.isEmpty()) {
                    mapCommands(module, paths);
                    mapChildren(module, paths);
                } else {
                    paths.add(group);
                    mapCommands(module, paths);
                    mapChildren(module, paths);

                    paths.remove(paths.size() - 1);
                }
            }
        }

        private void mapChildren(CommandModule module, List<String> paths) {
            for (CommandModule child : module.children) {
                mapModule(child, paths);
            }
        }

        private void mapCommands(CommandModule module, List<String> paths) {
            for (Command command : module.commands) {
                if (command.aliases.isEmpty()) {
                    rootNode.addCommand(command, paths, 0);
                    continue;
                }

                for (String alias : command.aliases) {
                    if (alias.isEmpty()) {
                        if (paths.isEmpty()) {
                            continue;
                        }

                        rootNode.addCommand(command, paths, 0);
                    } else {
                        paths.add(alias);
                        rootNode.addCommand(command, paths, 0);
                        paths.remove(paths.size() - 1);
                    }
                }
            }
        }

        public CommandMap build() {
            Preconditions.checkState(!invalidState, "CommandMap has been put into an invalid state");
            return new CommandMap(rootNode.build());
        }
    }
}
