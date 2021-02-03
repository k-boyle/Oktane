package kboyle.oktane.benchmark;

import kboyle.octane.core.BeanProvider;
import kboyle.octane.core.module.Command;
import kboyle.octane.core.module.CommandModuleFactory;
import kboyle.octane.core.module.Module;
import kboyle.octane.core.parsers.DefaultArgumentParser;
import kboyle.octane.core.parsers.PrimitiveTypeParser;
import kboyle.octane.core.results.Result;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
public class ArgumentParserBenchmark {
    private final DefaultArgumentParser argumentParser = new DefaultArgumentParser(PrimitiveTypeParser.DEFAULT_PARSERS);

    private final Module module = CommandModuleFactory.create(
        BenchmarkCommandContext.class,
        BenchmarkModule.class,
        BeanProvider.get()
    );

    private final Map<String, Command> commands = module.commands().stream()
        .collect(Collectors.toMap(Command::name, Function.identity()));

    @Benchmark
    public Result commandNotParameters() {
        return argumentParser.parse(new BenchmarkCommandContext(), commands.get("a"), "", 0);
    }

    @Benchmark
    public Result commandOneParameter() {
        return argumentParser.parse(new BenchmarkCommandContext(), commands.get("b"), "abc", 0);
    }

    @Benchmark
    public Result commandRemainderParameter() {
        return argumentParser.parse(new BenchmarkCommandContext(), commands.get("c"), "abc def ghi", 0);
    }

    @Benchmark
    public Result commandFiveParameters() {
        return argumentParser.parse(new BenchmarkCommandContext(), commands.get("f"), "abc def ghi jkl mno", 0);
    }
}
