package kboyle.oktane.reactive.parsers;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.mapping.CommandMatch;
import kboyle.oktane.reactive.results.argumentparser.ArgumentParserResult;

public interface ArgumentParser {
    ArgumentParserResult parse(CommandContext context, CommandMatch commandMatch, String input);
}
