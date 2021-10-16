package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.execution.ModuleBase;
import com.github.kboyle.oktane.core.parsing.TypeParserProvider;
import com.google.common.base.Preconditions;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.kboyle.oktane.core.Utilities.Functions.doNothing;

@FunctionalInterface
public interface CommandModulesFactory {
    Stream<CommandModule> createModules(TypeParserProvider typeParserProvider);

    @SafeVarargs
    static <CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> CommandModulesFactory classes(Class<MODULE>... moduleClasses) {
        return classes(doNothing(), moduleClasses);
    }

    @SafeVarargs
    static <CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> CommandModulesFactory classes(
            Consumer<CommandModule.Builder> builderConsumer,
            Class<MODULE>... moduleClasses) {

        Preconditions.checkNotNull(moduleClasses, "moduleClasses cannot be null");
        Preconditions.checkNotNull(builderConsumer, "builderConsumer cannot be null");

        return new ClassCommandModulesFactory<>(List.of(moduleClasses), builderConsumer);
    }
}
