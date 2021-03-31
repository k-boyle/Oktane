package kboyle.oktane.core.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.typeparser.TypeParserResult;

public class CharTypeParser implements TypeParser<Character> {
    @Override
    public TypeParserResult parse(CommandContext context, String input) {
        if (input.length() != 1) {
            return failure("A char can only be a single character");
        }
        return success(input.charAt(0));
    }
}
