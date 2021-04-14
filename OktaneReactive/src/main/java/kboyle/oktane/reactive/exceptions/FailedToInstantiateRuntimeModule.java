package kboyle.oktane.reactive.exceptions;

public class FailedToInstantiateRuntimeModule extends RuntimeException {
    public FailedToInstantiateRuntimeModule(Exception exception) {
        super(exception);
    }
}
