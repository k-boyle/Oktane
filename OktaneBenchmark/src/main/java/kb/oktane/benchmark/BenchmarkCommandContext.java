package kb.oktane.benchmark;

import kb.octane.core.BeanProvider;
import kb.octane.core.CommandContext;

public class BenchmarkCommandContext extends CommandContext {
    public BenchmarkCommandContext() {
        super(BeanProvider.get());
    }
}
