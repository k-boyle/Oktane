package kboyle.oktane.core.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.Result;

public interface ArgumentParser {
    Result parse(CommandContext context, String input, int index);
}
