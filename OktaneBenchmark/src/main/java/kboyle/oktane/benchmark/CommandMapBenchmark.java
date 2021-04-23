package kboyle.oktane.benchmark;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.mapping.CommandMap;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.factory.CommandModuleFactory;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
public class CommandMapBenchmark {
    private static final CommandModule MODULE = new CommandModuleFactory<BenchmarkContext, ModuleBase<BenchmarkContext>>(BeanProvider.empty(), ImmutableMap.of())
        .create(GeneratedBenchmarkModule.class);

    private static final CommandMap COMMAND_MAP = CommandMap.builder()
        .map(MODULE)
        .build();

    @Benchmark
    public ImmutableList<CommandMatch> commandNotFound() {
        return COMMAND_MAP.findCommands("not found");
    }
}
