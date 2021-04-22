package kboyle.oktane.reactive.parsers;

import com.google.common.base.Preconditions;

public class CharReactiveTypeParser extends PrimitiveTypeParser<Character> {
    public CharReactiveTypeParser() {
        super(Character.class, CharReactiveTypeParser::parse);
    }

    private static char parse(String input) {
        Preconditions.checkState(input.length() == 1, "A char can only be a single character");
        return input.charAt(0);
    }
}
