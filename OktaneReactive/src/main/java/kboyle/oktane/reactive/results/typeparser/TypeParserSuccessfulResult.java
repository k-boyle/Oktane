package kboyle.oktane.reactive.results.typeparser;

import kboyle.oktane.reactive.results.SuccessfulResult;

public record TypeParserSuccessfulResult<T>(T value) implements TypeParserResult<T>, SuccessfulResult {
}
