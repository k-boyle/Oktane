package kboyle.oktane.benchmark;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.BenchmarkCommandBuilder;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.parsers.DefaultArgumentParser;
import kboyle.oktane.core.results.argumentparser.ArgumentParserResult;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
public class ArgumentParserScalingBenchmark {
    private static final DefaultArgumentParser ARGUMENT_PARSER = new DefaultArgumentParser(ImmutableMap.of());

    @Param({"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"})
    public int parameterCount;

    private String input;
    private CommandMatch match;
    private BenchmarkCommandContext context;

    @Setup
    public void setup() {
        input = " ";

        BenchmarkCommandBuilder benchmarkCommandBuilder = new BenchmarkCommandBuilder();
        for (int i = 0; i < parameterCount; i++) {
            benchmarkCommandBuilder.withParameter();
            input += (i + " ");
        }

        Command command = benchmarkCommandBuilder.create();
        match = new CommandMatch(command, 0, 0, 1);
        context = new BenchmarkCommandContext();
    }

    @Benchmark
    public ArgumentParserResult benchmark() {
        ArgumentParserResult result = ARGUMENT_PARSER.parse(context, match, input);
        Preconditions.checkState(result.success());
        return result;
    }
}
