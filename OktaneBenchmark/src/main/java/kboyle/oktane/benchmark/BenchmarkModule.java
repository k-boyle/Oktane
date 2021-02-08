package kboyle.oktane.benchmark;

import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.module.annotations.CommandDescription;
import kboyle.oktane.core.module.annotations.ParameterDescription;
import kboyle.oktane.core.results.command.CommandResult;

public class BenchmarkModule extends CommandModuleBase<BenchmarkCommandContext> {
    @CommandDescription(aliases = "a")
    public CommandResult a() {
        return nop();
    }

    @CommandDescription(aliases = "b")
    public CommandResult b(String arg1) {
        return nop();
    }

    @CommandDescription(aliases = "c")
    public CommandResult c(@ParameterDescription(remainder = true) String arg1) {
        return nop();
    }

    @CommandDescription(aliases = "e")
    public CommandResult e(int arg1) {
        return nop();
    }

    @CommandDescription(aliases = "f")
    public CommandResult f(String one, String two, String three, String four, String five) {
        return nop();
    }
}
