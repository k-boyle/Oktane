package com.github.kboyle.oktane.core.result.tokeniser;

import com.github.kboyle.oktane.core.result.Result;

import java.util.List;

public interface TokeniserResult extends Result {
    List<String> tokens();
}
