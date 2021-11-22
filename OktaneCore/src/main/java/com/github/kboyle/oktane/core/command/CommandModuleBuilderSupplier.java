package com.github.kboyle.oktane.core.command;

import java.util.function.Supplier;
import java.util.stream.Stream;

@FunctionalInterface
public interface CommandModuleBuilderSupplier extends Supplier<Stream<CommandModule.Builder>> {
}
