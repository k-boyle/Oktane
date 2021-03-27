package kboyle.oktane.benchmark;

import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModuleFactory;
import kboyle.oktane.core.module.Module;
import kboyle.oktane.core.parsers.GenericArgumentParser;
import kboyle.oktane.core.parsers.PrimitiveTypeParserFactory;
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
    private final GenericArgumentParser argumentParser = new GenericArgumentParser(PrimitiveTypeParserFactory.create());

    private final Module module = CommandModuleFactory.create(
        BenchmarkModule.class,
        BeanProvider.empty(),
        PrimitiveTypeParserFactory.create(),
        argumentParserByClass);

    private final Map<String, Command> commands = module.commands().stream()
        .collect(Collectors.toMap(Command::name, Function.identity()));

    @Benchmark
    public ArgumentParserResult commandNoParameters() {
        return argumentParser.parse(new BenchmarkCommandContext(), new CommandMatch(commands.get("a"), 0, -1, 0), "");
    }

    @Benchmark
    public ArgumentParserResult commandOneParameter() {
        return argumentParser.parse(new BenchmarkCommandContext(), new CommandMatch(commands.get("b"), 0, -1, 0), "abc");
    }

    @Benchmark
    public ArgumentParserResult commandRemainderParameter() {
        return argumentParser.parse(new BenchmarkCommandContext(), new CommandMatch(commands.get("c"), 0, -1, 0), "abc def ghi");
    }

    @Benchmark
    public ArgumentParserResult commandFiveParameters() {
        return argumentParser.parse(new BenchmarkCommandContext(), new CommandMatch(commands.get("f"), 0, -1, 0), "abc def ghi jkl mno");
    }
}
