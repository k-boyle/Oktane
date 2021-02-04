package kboyle.octane.core.mapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kboyle.octane.core.module.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CommandMapNode {
    private final ImmutableMap<String, List<Command>> commandsByAlias;
    private final ImmutableMap<String, CommandMapNode> nodeByAlias;

    private CommandMapNode(Map<String, List<Command>> commandsByAlias, Map<String, CommandMapNode> nodeByAlias) {
        this.commandsByAlias = ImmutableMap.copyOf(commandsByAlias);
        this.nodeByAlias = ImmutableMap.copyOf(nodeByAlias);
    }

    public ImmutableList<CommandSearchResult> findCommands(String input) {
        ImmutableList.Builder<CommandSearchResult> results = ImmutableList.builder();
        findCommands(results, 0, input, 0);
        return results.build();
    }

    private void findCommands(ImmutableList.Builder<CommandSearchResult> results, int pathLength, String input, int index) {
        if (input.length() == 0 || index == input.length()) {
            return;
        }

        int nextSpace = input.indexOf(" ", index);

        if (nextSpace == -1) {
            String segment = index == 0 ? input : input.substring(index);
            handleSegmentAsAlias(results, segment, input, pathLength, input.length() - 1);
        } else {
            String segment = input.substring(index, nextSpace);
            handleSegmentAsAlias(results, segment, input, pathLength, nextSpace + 1);

            CommandMapNode commandMapNode = nodeByAlias.get(segment);
            if (commandMapNode != null) {
                pathLength++;
                commandMapNode.findCommands(results, pathLength, input, nextSpace + 1);
            }
        }
    }

    private void handleSegmentAsAlias(ImmutableList.Builder<CommandSearchResult> results, String segment, String input, int pathLength, int index) {
        List<Command> commands = commandsByAlias.get(segment);
        if (commands != null) {
            pathLength++;
            for (Command command : commands) {
                results.add(new CommandSearchResult(command, pathLength, input, index));
            }
        }
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private final Map<String, List<Command>> commandsByAlias;
        private final Map<String, Builder> nodeByAlias;

        private Builder() {
            this.commandsByAlias = new HashMap<>();
            this.nodeByAlias = new HashMap<>();
        }

        public Builder addCommand(Command command, List<String> paths, int index) {
            Preconditions.checkState(!paths.isEmpty(), "Cannot map pathless commands to root");

            String path = paths.get(index);
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
            } else {
                nodeByAlias.compute(path, (p, node) -> {
                    if (node == null) {
                        node = CommandMapNode.builder();
                    }

                    node.addCommand(command, paths, index + 1);
                    return node;
                });
            }

            return this;
        }

        private void assertUniqueCommand(Command command, String path, List<Command> commands) {
            for (Command otherCommand : commands) {
                Command.Signature commandSignature = command.signature();
                Command.Signature otherCommandSignature = otherCommand.signature();

                Preconditions.checkState(
                    !commandSignature.equals(otherCommandSignature),
                    "Multiple matching signatures, %s for path %s",
                    commandSignature,
                    path
                );
            }
        }

        public CommandMapNode build() {
            Map<String, CommandMapNode> builtNodeByAlias = this.nodeByAlias.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, es -> es.getValue().build()));
            return new CommandMapNode(commandsByAlias, builtNodeByAlias);
        }
    }
}
