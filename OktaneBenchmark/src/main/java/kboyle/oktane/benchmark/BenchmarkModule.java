package kboyle.oktane.benchmark;

import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.results.command.CommandResult;

public class BenchmarkModule extends CommandModuleBase<BenchmarkCommandContext> {
    @Aliases("a")
    public CommandResult a() {
        return nop();
    }

    @Aliases("b")
    public CommandResult b(String arg1) {
        return nop();
    }

    @Aliases("c")
    public CommandResult c(String arg1) {
        return nop();
    }

    @Aliases("e")
    public CommandResult e(int arg1) {
        return nop();
    }

    @Aliases("f")
    public CommandResult f(String one, String two, String three, String four, String five) {
        return nop();
    }
}
