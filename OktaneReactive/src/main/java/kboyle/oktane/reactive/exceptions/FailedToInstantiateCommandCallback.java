package kboyle.oktane.reactive.exceptions;

public class FailedToInstantiateCommandCallback extends RuntimeException {
    public FailedToInstantiateCommandCallback(Exception exception) {
        super(exception);
    }
}
