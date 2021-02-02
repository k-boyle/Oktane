package kb.octane.core.mapping;

import kb.octane.core.module.Command;

public record CommandSearchResult(Command command, int pathLength, String input, int offset) {
}
