package kboyle.oktane.benchmark;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.mapping.CommandMap;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.CommandModuleFactory;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
public class CommandMapBenchmark {
    private final CommandMap commandMap = CommandMap.builder()
        .map(CommandModuleFactory.create(BenchmarkModule.class, BeanProvider.empty()))
        .build();

    @Benchmark
    public ImmutableList<CommandMatch> currentCommandNoParameters() {
        return commandMap.findCommands("a");
    }

    @Benchmark
    public ImmutableList<CommandMatch> currentCommandOneParameter() {
        return commandMap.findCommands("n abc");
    }

    @Benchmark
    public ImmutableList<CommandMatch> currentCommandRemainderParameter() {
        return commandMap.findCommands("c abc def ghi");
    }

    @Benchmark
    public ImmutableList<CommandMatch> currentCommandNotFound() {
        return commandMap.findCommands("notfound");
    }

    @Benchmark
    public ImmutableList<CommandMatch> currentCommandFiveParameters() {
        return commandMap.findCommands("f abc def ghi jkl mno");
    }

    @Benchmark
    public ImmutableList<CommandMatch> newCommandNoParameters() {
        return commandMap.findCommands("a");
    }

    @Benchmark
    public ImmutableList<CommandMatch> newCommandOneParameter() {
        return commandMap.findCommands("n abc");
    }

    @Benchmark
    public ImmutableList<CommandMatch> newCommandRemainderParameter() {
        return commandMap.findCommands("c abc def ghi");
    }

    @Benchmark
    public ImmutableList<CommandMatch> newCommandFiveParameters() {
        return commandMap.findCommands("f abc def ghi jkl mno");
    }

    @Benchmark
    public ImmutableList<CommandMatch> newCommandNotFound() {
        return commandMap.findCommands("notfound");
    }
}
