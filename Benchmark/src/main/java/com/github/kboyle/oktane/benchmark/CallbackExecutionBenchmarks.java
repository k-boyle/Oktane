package com.github.kboyle.oktane.benchmark;

import com.github.kboyle.oktane.core.execution.AbstractCommandCallback;
import com.github.kboyle.oktane.core.execution.CommandContext;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class CallbackExecutionBenchmarks {
    private static final AbstractCommandCallback<CommandContext, BenchmarkModuleWithoutContext> NO_PARAMETERS;
    private static final AbstractCommandCallback<CommandContext, BenchmarkModuleWithoutContext> ONE_PARAMETER;
    private static final AbstractCommandCallback<CommandContext, BenchmarkModuleWithoutContext> TWO_PARAMETERS;
    private static final Object[] EMPTY = new Object[0];
    private static final Object[] ONE = new Object[] { "one" };
    private static final Object[] TWO = new Object[] { "one", "TWO" };
    private static final BenchmarkContext EMPTY_CONTEXT = new BenchmarkContext(EMPTY);
    private static final BenchmarkContext ONE_CONTEXT = new BenchmarkContext(ONE);
    private static final BenchmarkContext TWO_CONTEXT = new BenchmarkContext(TWO);

    static {
        try {
            NO_PARAMETERS = AbstractCommandCallback.create(BenchmarkModuleWithoutContext.class, BenchmarkModuleWithoutContext.class.getDeclaredMethod("none"));
            ONE_PARAMETER = AbstractCommandCallback.create(BenchmarkModuleWithoutContext.class, BenchmarkModuleWithoutContext.class.getDeclaredMethod("one", String.class));
            TWO_PARAMETERS = AbstractCommandCallback.create(BenchmarkModuleWithoutContext.class, BenchmarkModuleWithoutContext.class.getDeclaredMethod("two", String.class, String.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void directNoParameters(Blackhole blackhole) {
        var module = new BenchmarkModuleWithContext(EMPTY_CONTEXT);
        blackhole.consume(module.none());
    }

    @Benchmark
    public void directOneParameter(Blackhole blackhole) {
        var module = new BenchmarkModuleWithContext(ONE_CONTEXT);
        blackhole.consume(module.one("one"));
    }

    @Benchmark
    public void directTwoParameters(Blackhole blackhole) {
        var module = new BenchmarkModuleWithContext(TWO_CONTEXT);
        blackhole.consume(module.two("one", "two"));
    }

    @Benchmark
    public void generatedNoParameters(Blackhole blackhole) {
        blackhole.consume(NO_PARAMETERS.execute(EMPTY_CONTEXT));
    }

    @Benchmark
    public void generatedOneParameter(Blackhole blackhole) {
        blackhole.consume(ONE_PARAMETER.execute(ONE_CONTEXT));
    }

    @Benchmark
    public void generatedTwoParameters(Blackhole blackhole) {
        blackhole.consume(TWO_PARAMETERS.execute(TWO_CONTEXT));
    }
}
