package kboyle.oktane.reactive.mapping;

import kboyle.oktane.reactive.module.ReactiveCommand;

public record CommandMatch(ReactiveCommand command, int pathLength, int commandEnd, int argumentStart) {
}
