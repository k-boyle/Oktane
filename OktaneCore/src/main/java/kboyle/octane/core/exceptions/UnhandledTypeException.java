package kboyle.octane.core.exceptions;

import java.lang.reflect.Type;

public class UnhandledTypeException extends RuntimeException {
    public UnhandledTypeException(Type type) {
        super(String.format("Current %s is not handled", type));
    }
}
