package kboyle.oktane.benchmark;

import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;

public class BenchmarkCommandContext extends CommandContext {
    public BenchmarkCommandContext() {
        super(BeanProvider.get());
    }
}
