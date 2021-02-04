package kboyle.oktane.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

class BenchmarkRunner {
    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
//            .include(CommandExecutionBenchmark.class.getSimpleName())
            .include(CommandTotalExecutionBenchmark.class.getSimpleName())
//            .include(CommandMapBenchmark.class.getSimpleName())
//            .include(ArgumentParserBenchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(options).run();
    }
}
