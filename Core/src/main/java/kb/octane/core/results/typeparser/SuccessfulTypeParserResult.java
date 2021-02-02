package kb.octane.core.results.typeparser;

import kb.octane.core.results.SuccessfulResult;

public record SuccessfulTypeParserResult<T>(T value) implements TypeParserResult, SuccessfulResult {
}
