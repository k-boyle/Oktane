package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.argumentparser.ArgumentParserResult;

import java.util.List;

public interface ArgumentParser {
    ArgumentParserResult parse(CommandContext context, Command command, List<String> tokens);

    static ArgumentParser get() {
        return new DefaultArgumentParser();
    }
}
