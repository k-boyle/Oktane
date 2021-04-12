package kboyle.oktane.reactive.results.tokeniser;

import kboyle.oktane.reactive.results.Result;

import java.util.List;

public interface TokeniserResult extends Result {
    List<String> tokens();
}
