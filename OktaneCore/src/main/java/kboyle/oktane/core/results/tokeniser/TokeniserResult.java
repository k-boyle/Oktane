package kboyle.oktane.core.results.tokeniser;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.Result;

import java.util.List;

public interface TokeniserResult extends Result {
    List<String> tokens();
    Command command();
}
