package kboyle.oktane.reactive.parsers;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.argumentparser.ArgumentParserResult;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReactiveArgumentParser {
    Mono<ArgumentParserResult> parse(CommandContext context, ReactiveCommand command, List<String> tokens);
}
