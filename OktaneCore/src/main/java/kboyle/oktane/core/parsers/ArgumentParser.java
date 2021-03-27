package kboyle.oktane.core.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.results.argumentparser.ArgumentParserResult;

public interface ArgumentParser {
    ArgumentParserResult parse(CommandContext context, CommandMatch commandMatch, String input);
}
