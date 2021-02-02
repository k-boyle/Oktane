package kb.octane.core.results.typeparser;

import kb.octane.core.results.FailedResult;

public record FailedTypeParserResult(String reason) implements TypeParserResult, FailedResult {
}
