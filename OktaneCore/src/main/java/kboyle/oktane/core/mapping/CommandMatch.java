package kboyle.oktane.core.mapping;

import kboyle.oktane.core.module.Command;

public record CommandMatch(Command command, int commandEnd, int argumentStart) {
}
