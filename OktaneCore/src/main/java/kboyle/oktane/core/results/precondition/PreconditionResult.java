package kboyle.oktane.core.results.precondition;

import kboyle.oktane.core.results.Result;
import reactor.core.publisher.Mono;

public interface PreconditionResult extends Result {
    default Mono<PreconditionResult> mono() {
        return Mono.just(this);
    }
}
