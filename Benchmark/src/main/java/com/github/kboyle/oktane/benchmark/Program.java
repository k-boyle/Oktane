package com.github.kboyle.oktane.benchmark;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class Program {
    public static void main(String[] args) throws RunnerException {
        var options = new OptionsBuilder()
            .include(CallbackExecutionBenchmarks.class.getSimpleName())
            .resultFormat(ResultFormatType.CSV)
            .forks(1)
            .timeUnit(TimeUnit.NANOSECONDS)
            .mode(Mode.AverageTime)
            .build();

        new Runner(options).run();
    }
}
