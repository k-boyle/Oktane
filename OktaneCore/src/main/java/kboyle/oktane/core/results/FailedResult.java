package kboyle.oktane.core.results;

public interface FailedResult extends Result {
    String reason();

    @Override
    default boolean success() {
        return false;
    }
}
