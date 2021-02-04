package kboyle.octane.core.results.typeparser;

import kboyle.octane.core.results.SuccessfulResult;

public record SuccessfulTypeParserResult<T>(T value) implements TypeParserResult, SuccessfulResult {
}
