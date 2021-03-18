package kboyle.oktane.benchmark;

import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModuleFactory;
import kboyle.oktane.core.module.Module;
import kboyle.oktane.core.parsers.DefaultArgumentParser;
import kboyle.oktane.core.parsers.PrimitiveTypeParser;
import kboyle.oktane.core.results.argumentparser.ArgumentParserResult;
import org.openjdk.jmh.annotations.*;

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
        BenchmarkModule.class,
        BeanProvider.empty()
    );

    private final Map<String, Command> commands = module.commands().stream()
        .collect(Collectors.toMap(Command::name, Function.identity()));

    @Benchmark
    public ArgumentParserResult commandNotParameters() {
        return argumentParser.parse(new BenchmarkCommandContext(), commands.get("a"), "", 0);
    }

    @Benchmark
    public ArgumentParserResult commandOneParameter() {
        return argumentParser.parse(new BenchmarkCommandContext(), commands.get("b"), "abc", 0);
    }

    @Benchmark
    public ArgumentParserResult commandRemainderParameter() {
        return argumentParser.parse(new BenchmarkCommandContext(), commands.get("c"), "abc def ghi", 0);
    }

    @Benchmark
    public ArgumentParserResult commandFiveParameters() {
        return argumentParser.parse(new BenchmarkCommandContext(), commands.get("f"), "abc def ghi jkl mno", 0);
    }
}
