package kboyle.oktane.reactive.exceptions;

import kboyle.oktane.reactive.module.Precondition;

public class FailedToInstantiatePreconditionException extends RuntimeException {
    public FailedToInstantiatePreconditionException(Class<? extends Precondition> clazz, Exception ex) {
        super(String.format("Couldn't instantiate a precondition of type %s", clazz), ex);
    }
}
