package kboyle.oktane.reactive.exceptions;

public class MethodInvocationFailedException extends RuntimeException {
    public MethodInvocationFailedException(Exception ex) {
        super(ex);
    }
}
