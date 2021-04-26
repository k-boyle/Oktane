package kboyle.oktane.core.results.typeparser;

import kboyle.oktane.core.results.Result;
import reactor.core.publisher.Mono;

public interface TypeParserResult<T> extends Result {
    T value();

    default Mono<TypeParserResult<T>> mono() {
        return Mono.just(this);
    }
}
