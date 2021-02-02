package kb.octane.core.parsers;

import kb.octane.core.CommandContext;
import kb.octane.core.results.typeparser.FailedTypeParserResult;
import kb.octane.core.results.typeparser.SuccessfulTypeParserResult;
import kb.octane.core.results.typeparser.TypeParserResult;

@FunctionalInterface
public interface TypeParser<T> {
    TypeParserResult parse(CommandContext context, String input);

    default SuccessfulTypeParserResult<T> success(T value) {
        return new SuccessfulTypeParserResult<>(value);
    }

    default FailedTypeParserResult failure(String reason, Object... args) {
        return new FailedTypeParserResult(String.format(reason, args));
    }
}
