package kboyle.oktane.example.preconditions;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.results.precondition.PreconditionResult;

public class FailurePrecondition implements Precondition {
    private final int a;

    public FailurePrecondition(String[] args) {
        this.a = Integer.parseInt(args[0]);
    }

    @Override
    public PreconditionResult run(CommandContext context) {
        return failure("Failed because: %d", a);
    }
}
