package kboyle.oktane.benchmark;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.mapping.CommandMap;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.CommandModuleFactory;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
public class CommandMapBenchmark {
    private final CommandMap commandMap = CommandMap.builder()
        .map(CommandModuleFactory.create(BenchmarkModule.class, BeanProvider.empty(), Map.of(), argumentParserByClass))
        .build();

    @Benchmark
    public ImmutableList<CommandMatch> commandNoParameters() {
        return commandMap.findCommands("a");
    }

    @Benchmark
    public ImmutableList<CommandMatch> commandOneParameter() {
        return commandMap.findCommands("n abc");
    }

    @Benchmark
    public ImmutableList<CommandMatch> commandRemainderParameter() {
        return commandMap.findCommands("c abc def ghi");
    }

    @Benchmark
    public ImmutableList<CommandMatch> commandNotFound() {
        return commandMap.findCommands("notfound");
    }

    @Benchmark
    public ImmutableList<CommandMatch> commandFiveParameters() {
        return commandMap.findCommands("f abc def ghi jkl mno");
    }
}
