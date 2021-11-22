package com.github.kboyle.oktane.core.mapping;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.command.CommandModule;
import com.google.common.base.Preconditions;

import java.util.*;
import java.util.stream.Collectors;

class ImmutableCommandMapNode implements CommandMap  {
    private static final char SPACE = ' ';

    private final Map<String, List<Command>> commandsByAlias;
    private final Map<String, ImmutableCommandMapNode> nodeByAlias;

    private ImmutableCommandMapNode(ImmutableCommandMapNodeBuilder builder) {
        this.commandsByAlias = Map.copyOf(builder.commandsByAlias);
        this.nodeByAlias = builder.nodeByAlias.entrySet().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    Map.Entry::getKey,
                    entry -> new ImmutableCommandMapNode(entry.getValue())
                )
            );
    }

    static ImmutableCommandMapNode create(List<CommandModule> modules) {
        var rootNode = new ImmutableCommandMapNodeBuilder();
        for (var module : modules) {
            rootNode.mapModule(module);
        }

        return rootNode.build();
    }

    // todo checkState to checkArgument
    @Override
    public List<CommandMatch> findMatches(String input, int startIndex) {
        Preconditions.checkNotNull(input, "input cannot be null");
        Preconditions.checkArgument(startIndex >= 0, "startIndex cannot be negative");

        var results = new ArrayList<CommandMatch>();
        while (startIndex < input.length() && input.charAt(startIndex) == SPACE) {
            startIndex++;
        }

        findCommands(results, input, startIndex);
        return results;
    }

    private void findCommands(ArrayList<CommandMatch> results, String input, int index) {
        if (input.length() == 0 || index == input.length()) {
            return;
        }

        var nextSpace = input.indexOf(SPACE, index);

        if (nextSpace == -1) {
            var segment = index == 0 ? input : input.substring(index);
            var lastIndex = input.length() - 1;
            handleSegmentAsAlias(results, segment, lastIndex, lastIndex);
        } else {
            var segment = input.substring(index, nextSpace);
            handleSegmentAsAlias(results, segment, nextSpace - 1, nextSpace + 1);

            var commandMapNode = nodeByAlias.get(segment);
            if (commandMapNode != null) {
                commandMapNode.findCommands(results, input, nextSpace + 1);
            }
        }
    }

    private void handleSegmentAsAlias(ArrayList<CommandMatch> results, String segment, int commandEnd, int argumentStart) {
        var commands = commandsByAlias.get(segment);
        if (commands != null) {
            for (var command : commands) {
                results.add(new CommandMatch(command, commandEnd));
            }
        }
    }

    private static class ImmutableCommandMapNodeBuilder {
        private final Map<String, List<Command>> commandsByAlias;
        private final Map<String, ImmutableCommandMapNodeBuilder> nodeByAlias;

        private ImmutableCommandMapNodeBuilder() {
            this.commandsByAlias = new HashMap<>();
            this.nodeByAlias = new HashMap<>();
        }

        private void mapModule(CommandModule module) {
            mapModule(module, new ArrayList<>());
        }

        private void mapModule(CommandModule module, List<String> paths) {
            if (module.groups().isEmpty()) {
                mapCommands(module, paths);
                mapChildren(module, paths);
                return;
            }

            for (var group : module.groups()) {
                if (group.isEmpty()) {
                    mapCommands(module, paths);
                    mapChildren(module, paths);
                    continue;
                }

                Preconditions.checkState(!group.contains(" "), "Cannot map a space within a group");

                paths.add(group);
                mapCommands(module, paths);
                mapChildren(module, paths);
                paths.remove(paths.size() - 1);
            }
        }

        private void mapCommands(CommandModule module, List<String> paths) {
            for (var command : module.commands()) {
                if (command.aliases().isEmpty()) {
                    addCommand(command, paths, 0);
                    continue;
                }

                for (var alias : command.aliases()) {
                    if (alias.isEmpty()) {
                        if (paths.isEmpty()) {
                            continue;
                        }

                        addCommand(command, paths, 0);
                        continue;
                    }

                    Preconditions.checkState(!alias.contains(" "), "Cannot map a space within an alias \"%s\"", alias);

                    paths.add(alias);
                    addCommand(command, paths, 0);
                    paths.remove(paths.size() - 1);
                }
            }
        }

        private void mapChildren(CommandModule module, List<String> paths) {
            for (var child : module.children()) {
                mapModule(child, paths);
            }
        }

        private void addCommand(Command command, List<String> paths, int index) {
            Preconditions.checkState(!paths.isEmpty(), "Cannot map pathless commands to root");

            var path = paths.get(index);
            if (index == paths.size() - 1) {
                commandsByAlias.compute(path, (p, commands) -> {
                    if (commands != null) {
                        assertUniqueCommand(command, p, commands);
                    } else {
                        commands = new ArrayList<>();
                    }

                    commands.add(command);
                    return commands;
                });

                return;
            }

            nodeByAlias.compute(path, (p, node) -> {
                if (node == null) {
                    node = new ImmutableCommandMapNodeBuilder();
                }

                node.addCommand(command, paths, index + 1);
                return node;
            });
        }

        private void assertUniqueCommand(Command command, String path, List<Command> commands) {
            for (var otherCommand : commands) {
                var commandSignature = command.signature();
                var otherCommandSignature = otherCommand.signature();

                Preconditions.checkState(
                    !commandSignature.equals(otherCommandSignature),
                    "Multiple matching signatures, %s for path %s",
                    commandSignature,
                    path
                );
            }
        }

        private ImmutableCommandMapNode build() {
            return new ImmutableCommandMapNode(this);
        }
    }
}
