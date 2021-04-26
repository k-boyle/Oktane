package kboyle.oktane.core.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.argumentparser.ArgumentParserResult;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ArgumentParser {
    Mono<ArgumentParserResult> parse(CommandContext context, Command command, List<String> tokens);
}
