package kboyle.oktane.core.exceptions;

import java.lang.reflect.Method;

public class FailedToFindGeneratedCallbackException extends RuntimeException {
    public FailedToFindGeneratedCallbackException(Method method, String path) {
        super(String.format("Failed to find a class for method {} using {}", method, path));
    }
}
