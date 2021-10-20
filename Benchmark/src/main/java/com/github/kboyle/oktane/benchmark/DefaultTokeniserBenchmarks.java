package com.github.kboyle.oktane.benchmark;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.mapping.CommandMatch;
import com.github.kboyle.oktane.core.parsing.DefaultTokeniser;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class DefaultTokeniserBenchmarks {
    private static final DefaultTokeniser TOKENISER = new DefaultTokeniser();

    private static final Command NO_PARAMETERS = new BenchmarkCommandBuilder()
        .build();

    private static final Command ONE_PARAMETER = new BenchmarkCommandBuilder()
        .parameter(String.class, false)
        .build();

    private static final Command ONE_REMAINDER = new BenchmarkCommandBuilder()
        .parameter(String.class, true)
        .build();

    private static final Command TWO_PARAMETER = new BenchmarkCommandBuilder()
        .parameter(String.class, false)
        .parameter(String.class, false)
        .build();

    private static final Command GREEDY_PARAMETER = new BenchmarkCommandBuilder()
        .greedyParameter(int.class)
        .parameter(String.class, false)
        .build();

    private static final CommandMatch NO_PARAMETERS_MATCH = new CommandMatch(NO_PARAMETERS, 0);
    private static final CommandMatch ONE_PARAMETER_MATCH = new CommandMatch(ONE_PARAMETER, 0);
    private static final CommandMatch ONE_REMAINDER_MATCH = new CommandMatch(ONE_REMAINDER, 0);
    private static final CommandMatch TWO_PARAMETER_MATCH = new CommandMatch(TWO_PARAMETER, 0);
    private static final CommandMatch GREEDY_PARAMETER_MATCH = new CommandMatch(GREEDY_PARAMETER, 0);

    @Benchmark
    public void noParameters(Blackhole blackhole) {
        blackhole.consume(TOKENISER.tokenise("c ", NO_PARAMETERS_MATCH));
    }

    @Benchmark
    public void oneParameter(Blackhole blackhole) {
        blackhole.consume(TOKENISER.tokenise("c one", ONE_PARAMETER_MATCH));
    }

    @Benchmark
    public void oneRemainder(Blackhole blackhole) {
        blackhole.consume(TOKENISER.tokenise("c one two", ONE_REMAINDER_MATCH));
    }

    @Benchmark
    public void twoParameter(Blackhole blackhole) {
        blackhole.consume(TOKENISER.tokenise("c one two", TWO_PARAMETER_MATCH));
    }

    @Benchmark
    public void twoParameterQuoted(Blackhole blackhole) {
        blackhole.consume(TOKENISER.tokenise("c \"one two three\" four", TWO_PARAMETER_MATCH));
    }

    @Benchmark
    public void greedyParameterOneValue(Blackhole blackhole) {
        blackhole.consume(TOKENISER.tokenise("c 0 10", GREEDY_PARAMETER_MATCH));
    }

    @Benchmark
    public void greedyParameterFourValues(Blackhole blackhole) {
        blackhole.consume(TOKENISER.tokenise("c 0 10 20 30", GREEDY_PARAMETER_MATCH));
    }
}
