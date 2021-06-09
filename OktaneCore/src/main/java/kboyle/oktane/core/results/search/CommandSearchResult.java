package kboyle.oktane.core.results.search;

import kboyle.oktane.core.results.FailedResult;
import reactor.core.publisher.Mono;

public interface CommandSearchResult extends FailedResult {
    String reason();

    default Mono<CommandSearchResult> mono() {
        return Mono.just(this);
    }
}
