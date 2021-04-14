package kboyle.oktane.reactive.results;

public interface SuccessfulResult extends Result {
    @Override
    default boolean success() {
        return true;
    }
}
