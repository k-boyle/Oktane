package com.github.kboyle.oktane.core.result;

public interface ExceptionResult extends FailResult {
    Exception exception();
}
