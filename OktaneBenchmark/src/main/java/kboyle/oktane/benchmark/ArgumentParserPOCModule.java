package kboyle.oktane.benchmark;

import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.results.command.CommandResult;

public class ArgumentParserPOCModule extends CommandModuleBase<BenchmarkCommandContext> {
    @Aliases("five")
    public CommandResult five(String a, String b, String c, String d, String e, String f, String g) {
        return nop();
    }
}
