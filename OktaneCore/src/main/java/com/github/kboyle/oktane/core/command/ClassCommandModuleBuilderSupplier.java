package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.execution.ModuleBase;
import com.github.kboyle.oktane.core.parsing.TypeParserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class ClassCommandModuleBuilderSupplier<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> implements CommandModuleBuilderSupplier {
    private final TypeParserProvider typeParserProvider;
    private final CommandModuleClassSupplier<CONTEXT, MODULE> commandModuleClassSupplier;

    @Autowired
    public ClassCommandModuleBuilderSupplier(TypeParserProvider typeParserProvider, CommandModuleClassSupplier<CONTEXT, MODULE> commandModuleClassSupplier) {
        this.typeParserProvider = typeParserProvider;
        this.commandModuleClassSupplier = commandModuleClassSupplier;
    }

    @Override
    public Stream<CommandModule.Builder> get() {
        return null;
    }
}
