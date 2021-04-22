package kboyle.oktane.reactive.results.precondition;

import kboyle.oktane.reactive.results.Result;
import reactor.core.publisher.Mono;

public interface PreconditionResult extends Result {
    default Mono<PreconditionResult> mono() {
        return Mono.just(this);
    }
}
