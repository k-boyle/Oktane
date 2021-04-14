package kboyle.oktane.reactive.results;

public interface ExceptionResult extends FailedResult {
    Throwable exception();
}
