package kboyle.oktane.benchmark;

import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.results.Result;
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
public class CommandTotalExecutionBenchmark {
    private final CommandHandler<BenchmarkCommandContext> commandHandler = CommandHandler.builderForContext(BenchmarkCommandContext.class)
        .withBeanProvider(BeanProvider.get())
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
