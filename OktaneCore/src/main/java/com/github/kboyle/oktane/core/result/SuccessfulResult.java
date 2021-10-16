package com.github.kboyle.oktane.core.result;

public interface SuccessfulResult extends Result {
    @Override
    default boolean success() {
        return true;
    }
}
