package kboyle.oktane.core.exceptions;

public class FailedToInstantiateRuntimeModule extends RuntimeException {
    public FailedToInstantiateRuntimeModule(Exception exception) {
        super(exception);
    }
}
