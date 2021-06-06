package kboyle.oktane.example.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.processor.AutoWith;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

@AutoWith
public class TestTypeParser implements TypeParser<Integer> {
    @Override
    public Mono<TypeParserResult<Integer>> parse(CommandContext context, Command command, String input) {
        return success(10).mono();
    }
}
