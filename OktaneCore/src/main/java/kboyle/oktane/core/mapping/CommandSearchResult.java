package kboyle.oktane.core.mapping;

import kboyle.oktane.core.module.Command;

public record CommandSearchResult(Command command, int pathLength, String input, int offset) {
}
