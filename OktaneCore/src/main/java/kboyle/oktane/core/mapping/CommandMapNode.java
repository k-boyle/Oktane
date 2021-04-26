package kboyle.oktane.core.mapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.module.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CommandMapNode {
    private static final char SPACE = ' ';

    private final ImmutableMap<String, List<Command>> commandsByAlias;
    private final ImmutableMap<String, CommandMapNode> nodeByAlias;

    private CommandMapNode(Map<String, List<Command>> commandsByAlias, Map<String, CommandMapNode> nodeByAlias) {
        this.commandsByAlias = ImmutableMap.copyOf(commandsByAlias);
        this.nodeByAlias = ImmutableMap.copyOf(nodeByAlias);
    }

    public ImmutableList<CommandMatch> findCommands(String input, int index) {
        var results = ImmutableList.<CommandMatch>builder();
        while (input.charAt(index) == SPACE) {
            index++;
        }
        findCommands(results, input, index);
        return results.build();
    }

    private void findCommands(ImmutableList.Builder<CommandMatch> results, String input, int index) {
        if (input.length() == 0 || index == input.length()) {
            return;
        }

        var nextSpace = input.indexOf(" ", index);

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

    private void handleSegmentAsAlias(
            ImmutableList.Builder<CommandMatch> results,
            String segment,
            int commandEnd,
            int argumentStart) {
        var commands = commandsByAlias.get(segment);
        if (commands != null) {
            for (int i = 0, commandsSize = commands.size(); i < commandsSize; i++) {
                var command = commands.get(i);
                results.add(new CommandMatch(command, commandEnd, argumentStart));
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
            for (var otherCommand : commands) {
                var commandSignature = command.signature;
                var otherCommandSignature = otherCommand.signature;

                Preconditions.checkState(
                    !commandSignature.equals(otherCommandSignature),
                    "Multiple matching signatures, %s for path %s",
                    commandSignature,
                    path
                );
            }
        }

        public CommandMapNode build() {
            var builtNodeByAlias = this.nodeByAlias.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, es -> es.getValue().build()));
            return new CommandMapNode(commandsByAlias, builtNodeByAlias);
        }
    }
}
