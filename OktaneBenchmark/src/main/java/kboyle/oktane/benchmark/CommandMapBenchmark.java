package kboyle.oktane.benchmark;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.mapping.CommandMap;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.factory.CommandModuleFactory;
import kboyle.oktane.core.module.factory.PreconditionFactoryMap;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
public class CommandMapBenchmark {
    private static final CommandModule MODULE = new CommandModuleFactory<BenchmarkContext, ModuleBase<BenchmarkContext>>(BeanProvider.empty(), Map.of(), new PreconditionFactoryMap())
        .create(GeneratedBenchmarkModule.class);

    private static final CommandMap COMMAND_MAP = CommandMap.builder()
        .map(MODULE)
        .build();

    @Benchmark
    public ImmutableList<CommandMatch> commandNotFound() {
        return COMMAND_MAP.findCommands("notfound");
    }
}
