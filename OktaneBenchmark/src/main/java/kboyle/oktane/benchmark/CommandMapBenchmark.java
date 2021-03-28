package kboyle.oktane.benchmark;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.mapping.CommandMap;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.CommandModuleFactory;
import kboyle.oktane.core.module.Module;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
public class CommandMapBenchmark {
    private static final Module MODULE = new CommandModuleFactory(BeanProvider.empty(), Map.of()).create(BenchmarkModule.class);

    private static final CommandMap COMMAND_MAP = CommandMap.builder()
        .map(MODULE)
        .build();

    @Benchmark
    public ImmutableList<CommandMatch> commandNoParameters() {
        return COMMAND_MAP.findCommands("a");
    }

    @Benchmark
    public ImmutableList<CommandMatch> commandOneParameter() {
        return COMMAND_MAP.findCommands("n abc");
    }

    @Benchmark
    public ImmutableList<CommandMatch> commandRemainderParameter() {
        return COMMAND_MAP.findCommands("c abc def ghi");
    }

    @Benchmark
    public ImmutableList<CommandMatch> commandNotFound() {
        return COMMAND_MAP.findCommands("notfound");
    }

    @Benchmark
    public ImmutableList<CommandMatch> commandFiveParameters() {
        return COMMAND_MAP.findCommands("f abc def ghi jkl mno");
    }
}
