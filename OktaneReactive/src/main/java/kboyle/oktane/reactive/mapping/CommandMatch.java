package kboyle.oktane.reactive.mapping;

import kboyle.oktane.reactive.module.Command;

public record CommandMatch(Command command, int pathLength, int commandEnd, int argumentStart) {
}
