package kboyle.oktane.core.results.typeparser;

import kboyle.oktane.core.results.FailedResult;

public record FailedTypeParserResult(String reason) implements TypeParserResult, FailedResult {
}
