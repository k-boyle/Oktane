package kboyle.oktane.core.exceptions;

public class MissingTypeParserException extends RuntimeException {
    public MissingTypeParserException(Class<?> clazz) {
        super(String.format("Missing a TypeParser for type %s", clazz));
    }
}
