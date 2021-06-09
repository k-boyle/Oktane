package kboyle.oktane.core.results.tokeniser;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.Result;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TokeniserResult extends Result {
    List<String> tokens();
    Command command();

    default Mono<TokeniserResult> mono() {
        return Mono.just(this);
    }
}
