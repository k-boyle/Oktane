package kboyle.oktane.core.results.typeparser;

import kboyle.oktane.core.results.SuccessfulResult;

public record TypeParserSuccessfulResult<T>(T value) implements TypeParserResult<T>, SuccessfulResult {
}
