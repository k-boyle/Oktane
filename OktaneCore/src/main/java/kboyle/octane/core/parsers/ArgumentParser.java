package kboyle.octane.core.parsers;

import kboyle.octane.core.CommandContext;
import kboyle.octane.core.results.Result;

public interface ArgumentParser {
    Result parse(CommandContext context, String input, int index);
}
