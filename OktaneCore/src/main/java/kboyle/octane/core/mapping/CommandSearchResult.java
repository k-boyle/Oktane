package kboyle.octane.core.mapping;

import kboyle.octane.core.module.Command;

public record CommandSearchResult(Command command, int pathLength, String input, int offset) {
}
