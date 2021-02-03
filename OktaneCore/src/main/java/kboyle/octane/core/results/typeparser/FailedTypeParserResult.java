package kboyle.octane.core.results.typeparser;

import kboyle.octane.core.results.FailedResult;

public record FailedTypeParserResult(String reason) implements TypeParserResult, FailedResult {
}
