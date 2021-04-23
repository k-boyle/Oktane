package kboyle.oktane.core.exceptions;

public class FailedToInstantiateCommandCallback extends RuntimeException {
    public FailedToInstantiateCommandCallback(Exception exception) {
        super(exception);
    }
}
