package kboyle.oktane.benchmark;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

class BenchmarkRunner {
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
            .include(TokeniserBenchmark.class.getSimpleName())
            .include(CommandExecutionBenchmark.class.getSimpleName())
            .include(CommandMapBenchmark.class.getSimpleName())
            .resultFormat(ResultFormatType.CSV)
            .forks(1)
            .build();

        new Runner(options).run();
    }
}
