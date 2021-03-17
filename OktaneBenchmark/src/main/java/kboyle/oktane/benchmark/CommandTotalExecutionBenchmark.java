package kboyle.oktane.benchmark;

import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.results.Result;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
public class CommandTotalExecutionBenchmark {
    private final CommandHandler<BenchmarkCommandContext> commandHandler = CommandHandler.<BenchmarkCommandContext>builder()
        .withBeanProvider(BeanProvider.empty())
        .withModule(BenchmarkModule.class)
        .build();

    private final BenchmarkCommandContext context = new BenchmarkCommandContext();

    @Benchmark
    public Result commandNoParameters() {
        return commandHandler.execute("a", context);
    }

    @Benchmark
    public Result commandOneParameter() {
        return commandHandler.execute("b abc", context);
    }

    @Benchmark
    public Result commandRemainderParameter() {
        return commandHandler.execute("c abc def ghi", context);
    }

    @Benchmark
    public Result commandNotFound() {
        return commandHandler.execute("notfound", context);
    }

    @Benchmark
    public Result commandIntParameter() {
        return commandHandler.execute("e 10", context);
    }

    @Benchmark
    public Result commandFiveParameters() {
        return commandHandler.execute("f abc def ghi jkl mno", context);
    }
}
