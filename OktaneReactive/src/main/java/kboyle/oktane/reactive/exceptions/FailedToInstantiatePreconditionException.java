package kboyle.oktane.reactive.exceptions;

import kboyle.oktane.reactive.module.ReactivePrecondition;

public class FailedToInstantiatePreconditionException extends RuntimeException {
    public FailedToInstantiatePreconditionException(Class<? extends ReactivePrecondition> clazz, Exception ex) {
        super(String.format("Couldn't instantiate a precondition of type %s", clazz), ex);
    }
}
