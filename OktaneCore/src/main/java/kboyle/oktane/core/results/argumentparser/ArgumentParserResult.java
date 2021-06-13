package kboyle.oktane.core.results.argumentparser;

import kboyle.oktane.core.results.Result;
import reactor.core.publisher.Mono;

public interface ArgumentParserResult extends Result {
    Object[] parsedArguments();

    default Mono<ArgumentParserResult> mono() {
        return Mono.just(this);
    }
}
