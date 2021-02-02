package kb.oktane.benchmark;

import com.google.common.collect.ImmutableList;
import kb.octane.core.BeanProvider;
import kb.octane.core.mapping.CommandMap;
import kb.octane.core.mapping.CommandSearchResult;
import kb.octane.core.module.CommandModuleFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
public class CommandMapBenchmark {
    private final CommandMap commandMap = CommandMap.builder()
        .map(CommandModuleFactory.create(BenchmarkCommandContext.class, BenchmarkModule.class, BeanProvider.get()))
        .build();

    @Benchmark
    public ImmutableList<CommandSearchResult> currentCommandNoParameters() {
        return commandMap.findCommands("a");
    }

    @Benchmark
    public ImmutableList<CommandSearchResult> currentCommandOneParameter() {
        return commandMap.findCommands("n abc");
    }

    @Benchmark
    public ImmutableList<CommandSearchResult> currentCommandRemainderParameter() {
        return commandMap.findCommands("c abc def ghi");
    }

    @Benchmark
    public ImmutableList<CommandSearchResult> currentCommandNotFound() {
        return commandMap.findCommands("notfound");
    }

    @Benchmark
    public ImmutableList<CommandSearchResult> currentCommandFiveParameters() {
        return commandMap.findCommands("f abc def ghi jkl mno");
    }

    @Benchmark
    public ImmutableList<CommandSearchResult> newCommandNoParameters() {
        return commandMap.findCommands("a");
    }

    @Benchmark
    public ImmutableList<CommandSearchResult> newCommandOneParameter() {
        return commandMap.findCommands("n abc");
    }

    @Benchmark
    public ImmutableList<CommandSearchResult> newCommandRemainderParameter() {
        return commandMap.findCommands("c abc def ghi");
    }

    @Benchmark
    public ImmutableList<CommandSearchResult> newCommandFiveParameters() {
        return commandMap.findCommands("f abc def ghi jkl mno");
    }

    @Benchmark
    public ImmutableList<CommandSearchResult> newCommandNotFound() {
        return commandMap.findCommands("notfound");
    }
}
