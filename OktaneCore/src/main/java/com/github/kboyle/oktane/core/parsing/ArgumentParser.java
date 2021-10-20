package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.argumentparser.ArgumentParserResult;

public interface ArgumentParser {
    ArgumentParserResult parse(CommandContext context);

    static ArgumentParser get() {
        return new DefaultArgumentParser();
    }
}
