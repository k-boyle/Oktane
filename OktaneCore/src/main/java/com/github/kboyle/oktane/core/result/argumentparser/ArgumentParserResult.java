package com.github.kboyle.oktane.core.result.argumentparser;

import com.github.kboyle.oktane.core.result.Result;

public interface ArgumentParserResult extends Result {
    Object[] parsedArguments();
}
