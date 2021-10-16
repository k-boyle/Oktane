package com.github.kboyle.oktane.core.mapping;

import com.github.kboyle.oktane.core.command.Command;

public record CommandMatch(Command command, int commandEnd, int argumentStart) {
}
