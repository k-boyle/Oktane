package kb.oktane.benchmark;

import kb.octane.core.BeanProvider;
import kb.octane.core.CommandHandler;
import kb.octane.core.results.Result;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import reactor.core.publisher.Mono;

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
    public Mono<Result> commandNoParameters() {
        return commandHandler.executeAsync("a", context);
    }

    @Benchmark
    public Mono<Result> commandOneParameter() {
        return commandHandler.executeAsync("b abc", context);
    }

    @Benchmark
    public Mono<Result> commandRemainderParameter() {
        return commandHandler.executeAsync("c abc def ghi", context);
    }

    @Benchmark
    public Mono<Result> commandNotFound() {
        return commandHandler.executeAsync("notfound", context);
    }

    @Benchmark
    public Mono<Result> commandIntParameter() {
        return commandHandler.executeAsync("e 10", context);
    }

    @Benchmark
    public Mono<Result> commandFiveParameters() {
        return commandHandler.executeAsync("f abc def ghi jkl mno", context);
    }
}
