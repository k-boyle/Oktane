package kboyle.octane.core.results;

public interface FailedResult extends Result {
    default boolean isSuccess() {
        return false;
    }

    String reason();
}
