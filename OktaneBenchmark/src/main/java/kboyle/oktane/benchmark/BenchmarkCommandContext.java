package kboyle.oktane.benchmark;

import kboyle.octane.core.BeanProvider;
import kboyle.octane.core.CommandContext;

public class BenchmarkCommandContext extends CommandContext {
    public BenchmarkCommandContext() {
        super(BeanProvider.get());
    }
}
