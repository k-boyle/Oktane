package com.github.kboyle.oktane.benchmark;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.parsing.DefaultArgumentParser;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;

public class DefaultArgumentParserBenchmarks {
    private static final DefaultArgumentParser ARGUMENT_PARSER = new DefaultArgumentParser();

    private static final Command NO_PARAMETERS = new BenchmarkCommandBuilder()
        .build();

    private static final Command ONE_PARAMETER = new BenchmarkCommandBuilder()
        .parameter(String.class, false)
        .build();

    private static final Command TWO_PARAMETER = new BenchmarkCommandBuilder()
        .parameter(String.class, false)
        .parameter(String.class, false)
        .build();

    private static final Command GREEDY_PARAMETER = new BenchmarkCommandBuilder()
        .greedyParameter(int.class)
        .parameter(String.class, false)
        .build();
    
    private static final BenchmarkCommandContext NO_PARAMETERS_COMMAND_CONTEXT = new BenchmarkCommandContext(NO_PARAMETERS, List.of());
    private static final BenchmarkCommandContext ONE_PARAMETER_COMMAND_CONTEXT = new BenchmarkCommandContext(ONE_PARAMETER, List.of("parameter"));
    private static final BenchmarkCommandContext TWO_PARAMETERS_COMMAND_CONTEXT = new BenchmarkCommandContext(TWO_PARAMETER, List.of("parameter1", "parameter2"));
    private static final BenchmarkCommandContext GREEDY_PARAMETER_ONE_VALUE_COMMAND_CONTEXT = new BenchmarkCommandContext(GREEDY_PARAMETER, List.of("10", "parameter2"));
    private static final BenchmarkCommandContext GREEDY_PARAMETER_FOUR_VALUES_COMMAND_CONTEXT = new BenchmarkCommandContext(GREEDY_PARAMETER, List.of("10", "20", "30", "40", "parameter5"));

    @Benchmark
    public void noParameters(Blackhole blackhole) {
        blackhole.consume(ARGUMENT_PARSER.parse(NO_PARAMETERS_COMMAND_CONTEXT));
    }

    @Benchmark
    public void oneParameter(Blackhole blackhole) {
        blackhole.consume(ARGUMENT_PARSER.parse(ONE_PARAMETER_COMMAND_CONTEXT));
    }

    @Benchmark
    public void twoParameter(Blackhole blackhole) {
        blackhole.consume(ARGUMENT_PARSER.parse(TWO_PARAMETERS_COMMAND_CONTEXT));
    }

    @Benchmark
    public void greedyParameterOneValue(Blackhole blackhole) {
        blackhole.consume(ARGUMENT_PARSER.parse(GREEDY_PARAMETER_ONE_VALUE_COMMAND_CONTEXT));
    }

    @Benchmark
    public void greedyParameterFourValues(Blackhole blackhole) {
        blackhole.consume(ARGUMENT_PARSER.parse(GREEDY_PARAMETER_FOUR_VALUES_COMMAND_CONTEXT));
    }
}
