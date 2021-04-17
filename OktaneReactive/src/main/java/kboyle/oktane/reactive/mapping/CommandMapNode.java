package kboyle.oktane.reactive.mapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.reactive.module.ReactiveCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CommandMapNode {
    private static final char SPACE = ' ';

    private final ImmutableMap<String, List<ReactiveCommand>> commandsByAlias;
    private final ImmutableMap<String, CommandMapNode> nodeByAlias;

    private CommandMapNode(Map<String, List<ReactiveCommand>> commandsByAlias, Map<String, CommandMapNode> nodeByAlias) {
        this.commandsByAlias = ImmutableMap.copyOf(commandsByAlias);
        this.nodeByAlias = ImmutableMap.copyOf(nodeByAlias);
    }

    public ImmutableList<CommandMatch> findCommands(String input, int index) {
        ImmutableList.Builder<CommandMatch> results = ImmutableList.builder();
        while (input.charAt(index) == SPACE) {
            index++;
        }
        findCommands(results, 0, input, index);
        return results.build();
    }

    private void findCommands(ImmutableList.Builder<CommandMatch> results, int pathLength, String input, int index) {
        if (input.length() == 0 || index == input.length()) {
            return;
        }

        int nextSpace = input.indexOf(" ", index);

        if (nextSpace == -1) {
            String segment = index == 0 ? input : input.substring(index);
            int lastIndex = input.length() - 1;
            handleSegmentAsAlias(results, segment, pathLength, lastIndex, lastIndex);
        } else {
            String segment = input.substring(index, nextSpace);
            handleSegmentAsAlias(results, segment, pathLength, nextSpace - 1, nextSpace + 1);

            CommandMapNode commandMapNode = nodeByAlias.get(segment);
            if (commandMapNode != null) {
                pathLength++;
                commandMapNode.findCommands(results, pathLength, input, nextSpace + 1);
            }
        }
    }

    private void handleSegmentAsAlias(
            ImmutableList.Builder<CommandMatch> results,
            String segment,
            int pathLength,
            int commandEnd,
            int argumentStart) {
        List<ReactiveCommand> commands = commandsByAlias.get(segment);
        if (commands != null) {
            pathLength++;
            for (int i = 0, commandsSize = commands.size(); i < commandsSize; i++) {
                ReactiveCommand command = commands.get(i);
                results.add(new CommandMatch(command, pathLength, commandEnd, argumentStart));
            }
        }
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private final Map<String, List<ReactiveCommand>> commandsByAlias;
        private final Map<String, Builder> nodeByAlias;

        private Builder() {
            this.commandsByAlias = new HashMap<>();
            this.nodeByAlias = new HashMap<>();
        }

        public Builder addCommand(ReactiveCommand command, List<String> paths, int index) {
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

        private void assertUniqueCommand(ReactiveCommand command, String path, List<ReactiveCommand> commands) {
            for (ReactiveCommand otherCommand : commands) {
                ReactiveCommand.Signature commandSignature = command.signature;
                ReactiveCommand.Signature otherCommandSignature = otherCommand.signature;

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
