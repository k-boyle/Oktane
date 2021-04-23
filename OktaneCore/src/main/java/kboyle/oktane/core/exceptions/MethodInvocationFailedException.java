package kboyle.oktane.core.exceptions;

public class MethodInvocationFailedException extends RuntimeException {
    public MethodInvocationFailedException(Exception ex) {
        super(ex);
    }
}
