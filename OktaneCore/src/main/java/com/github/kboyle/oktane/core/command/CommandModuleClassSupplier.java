package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.execution.ModuleBase;

import java.util.function.Supplier;
import java.util.stream.Stream;

@FunctionalInterface
public interface CommandModuleClassSupplier<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> extends Supplier<Stream<Class<? extends MODULE>>> {
}
