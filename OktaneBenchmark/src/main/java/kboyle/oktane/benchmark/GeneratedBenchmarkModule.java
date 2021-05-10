package kboyle.oktane.benchmark;

import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.results.command.CommandResult;

public class GeneratedBenchmarkModule extends ModuleBase<BenchmarkContext> {
    @Aliases("none")
    public CommandResult noParameters() {
        return nop();
    }

    @Aliases("one")
    public CommandResult oneParameter(String a) {
        return nop();
    }

    @Aliases("two")
    public CommandResult twoParameters(String a, String b) {
        return nop();
    }
}
