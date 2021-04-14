package kboyle.oktane.reactive.exceptions;

import java.io.IOException;

public class RuntimeIOException extends RuntimeException {
    public RuntimeIOException(IOException exception) {
        super(exception);
    }
}
