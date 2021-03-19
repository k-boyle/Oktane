package kboyle.oktane.core.results;

public interface SuccessfulResult extends Result {
    @Override
    default boolean success() {
        return true;
    }
}
