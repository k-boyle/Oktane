package kboyle.octane.core.exceptions;

public class FailedToInstantiateRuntimeModule extends RuntimeException {
    public FailedToInstantiateRuntimeModule(Exception exception) {
        super(exception);
    }
}
