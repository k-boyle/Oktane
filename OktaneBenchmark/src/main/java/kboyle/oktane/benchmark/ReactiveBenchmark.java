package kboyle.oktane.benchmark;

import kboyle.oktane.reactive.ReactiveCommandHandler;
import kboyle.oktane.reactive.results.Result;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
public class ReactiveBenchmark {
    private static final ReactiveCommandHandler<ReactiveContext> COMMAND_HANDLER = ReactiveCommandHandler.<ReactiveContext>builder()
        .withModule(ReactiveModule.class)
        .build();

    @Benchmark
    public Result execution() {
        return COMMAND_HANDLER.push("echo hi", new ReactiveContext()).block();
    }
}
