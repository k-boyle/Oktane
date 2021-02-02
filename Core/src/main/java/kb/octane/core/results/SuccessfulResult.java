package kb.octane.core.results;

public interface SuccessfulResult extends Result {
    @Override
    default boolean isSuccess() {
        return true;
    }
}
