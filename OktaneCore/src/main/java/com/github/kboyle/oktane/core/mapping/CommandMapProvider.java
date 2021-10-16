package com.github.kboyle.oktane.core.mapping;

import com.github.kboyle.oktane.core.command.CommandModule;

import java.util.List;

@FunctionalInterface
public interface CommandMapProvider {
    CommandMap create(List<CommandModule> modules);
}
