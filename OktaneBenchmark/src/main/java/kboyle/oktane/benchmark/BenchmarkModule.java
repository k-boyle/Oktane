package kboyle.oktane.benchmark;

import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.results.command.CommandResult;

public class BenchmarkModule extends CommandModuleBase<BenchmarkCommandContext> {
    public CommandResult a() {
        return nop();
    }

    public CommandResult b(String arg1) {
        return nop();
    }

    public CommandResult c(String arg1) {
        return nop();
    }

    public CommandResult e(int arg1) {
        return nop();
    }

    public CommandResult f(String one, String two, String three, String four, String five) {
        return nop();
    }
}
