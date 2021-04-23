package kboyle.oktane.core.parsers;

import com.google.common.base.Preconditions;

public class CharTypeParser extends PrimitiveTypeParser<Character> {
    public CharTypeParser() {
        super(Character.class, CharTypeParser::parse);
    }

    private static char parse(String input) {
        Preconditions.checkState(input.length() == 1, "A char can only be a single character");
        return input.charAt(0);
    }
}
