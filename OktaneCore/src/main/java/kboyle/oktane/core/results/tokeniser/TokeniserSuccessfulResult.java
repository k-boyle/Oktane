package kboyle.oktane.core.results.tokeniser;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.SuccessfulResult;

import java.util.List;

public record TokeniserSuccessfulResult(Command command, List<String> tokens) implements TokeniserResult, SuccessfulResult {
}
