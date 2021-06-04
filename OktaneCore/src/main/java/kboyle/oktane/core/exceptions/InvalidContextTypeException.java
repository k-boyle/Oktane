package kboyle.oktane.core.exceptions;

public class InvalidContextTypeException extends RuntimeException {
    public InvalidContextTypeException(Class<?> expectedContextType, Class<?> actualContextType) {
        super(String.format("Expected context type %s but got %s", expectedContextType, actualContextType));
    }
}
