package com.github.kboyle.oktane.core.result;

// todo names
public interface FailResult extends Result {
    String failureReason();

    @Override
    default boolean success() {
        return false;
    }
}
