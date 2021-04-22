package kboyle.oktane.reactive.results.typeparser;

import kboyle.oktane.reactive.results.Result;
import reactor.core.publisher.Mono;

public interface TypeParserResult<T> extends Result {
    T value();

    default Mono<TypeParserResult<T>> mono() {
        return Mono.just(this);
    }
}
