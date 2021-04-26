package kboyle.oktane.benchmark;

import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.BenchmarkCommandBuilder;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.parsers.DefaultTokeniser;
import kboyle.oktane.core.results.tokeniser.TokeniserResult;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
public class TokeniserBenchmark {
    private static final DefaultTokeniser TOKENISER = new DefaultTokeniser();

    private static final Command NO_PARAMETERS = new BenchmarkCommandBuilder()
        .create();
    private static final CommandMatch NO_PARAMETERS_MATCH = new CommandMatch(NO_PARAMETERS, -1, 0);

    private static final Command ONE_PARAMETER = new BenchmarkCommandBuilder()
        .withParameter()
        .create();
    private static final CommandMatch ONE_PARAMETER_MATCH = new CommandMatch(ONE_PARAMETER, -1, 0);

    private static final Command ONE_REMAINDER = new BenchmarkCommandBuilder()
        .withRemainderParameter()
        .create();
    private static final CommandMatch ONE_REMAINDER_MATCH = new CommandMatch(ONE_REMAINDER, -1, 0);

    private static final Command TWO_PARAMETER = new BenchmarkCommandBuilder()
        .withParameter()
        .withParameter()
        .create();
    private static final CommandMatch TWO_PARAMETER_MATCH = new CommandMatch(TWO_PARAMETER, -1, 0);

    @Benchmark
    public TokeniserResult noParameters() {
        return TOKENISER.tokenise(" ", NO_PARAMETERS_MATCH);
    }

    @Benchmark
    public TokeniserResult oneParameter() {
        return TOKENISER.tokenise(" one", ONE_PARAMETER_MATCH);
    }

    @Benchmark
    public TokeniserResult oneRemainder() {
        return TOKENISER.tokenise(" one two", ONE_REMAINDER_MATCH);
    }

    @Benchmark
    public TokeniserResult twoParameter() {
        return TOKENISER.tokenise(" one two", TWO_PARAMETER_MATCH);
    }

    @Benchmark
    public TokeniserResult twoParameterQuoted() {
        return TOKENISER.tokenise(" \"one two three\" four", TWO_PARAMETER_MATCH);
    }
}
