package kboyle.oktane.core.exceptions;

import kboyle.oktane.core.module.Precondition;

public class FailedToInstantiatePreconditionException extends RuntimeException {
    public FailedToInstantiatePreconditionException(Class<? extends Precondition> cl, Exception ex) {
        super(String.format("Couldn't instantiate a precondition of type %s", cl), ex);
    }
}
