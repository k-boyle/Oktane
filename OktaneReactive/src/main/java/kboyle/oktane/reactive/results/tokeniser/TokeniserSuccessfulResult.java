package kboyle.oktane.reactive.results.tokeniser;

import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.SuccessfulResult;

import java.util.List;

public record TokeniserSuccessfulResult(ReactiveCommand command, List<String> tokens) implements TokeniserResult, SuccessfulResult {
}
