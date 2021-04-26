package kboyle.oktane.core.results;

public interface ExceptionResult extends FailedResult {
    Throwable exception();
}
