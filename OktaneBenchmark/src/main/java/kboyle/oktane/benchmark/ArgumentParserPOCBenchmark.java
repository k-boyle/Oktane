package kboyle.oktane.benchmark;

import com.google.common.base.Preconditions;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.parsers.GenericArgumentParser;
import kboyle.oktane.core.parsers.PrimitiveTypeParserFactory;
import kboyle.oktane.core.results.argumentparser.ArgumentParserResult;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
public class ArgumentParserPOCBenchmark {
    private static final CommandHandler<BenchmarkCommandContext> COMMAND_HANDLER = CommandHandler.<BenchmarkCommandContext>builder()
        .withBeanProvider(BeanProvider.empty())
        .withModule(ArgumentParserPOCModule.class)
        .build();

    private static final Command COMMAND = COMMAND_HANDLER.commands().findFirst().get();

    private static final POCArgumentParser POC_ARGUMENT_PARSER = new POCArgumentParser(COMMAND, PrimitiveTypeParserFactory.create());
    private static final GenericArgumentParser FALLBACK_ARGUMENT_PARSER = new GenericArgumentParser(PrimitiveTypeParserFactory.create());
    private static final String INPUT = "stringa stringb stringc stringd stringe stringf stringg";
    private BenchmarkCommandContext CONTEXT;
    private CommandMatch COMMAND_MATCH;

    @Setup
    public void setup() {
        CONTEXT = new BenchmarkCommandContext();
        COMMAND_MATCH = new CommandMatch(COMMAND, 0, -1, 0);
    }

    @Benchmark
    public void poc() {
        ArgumentParserResult parse = POC_ARGUMENT_PARSER.parse(CONTEXT, COMMAND_MATCH, INPUT);
        Preconditions.checkState(parse.success(), parse);
        Object[] objects = parse.parsedArguments();
        Preconditions.checkState(objects[0].equals("stringa"), Arrays.toString(objects));
        Preconditions.checkState(objects[1].equals("stringb"), Arrays.toString(objects));
        Preconditions.checkState(objects[2].equals("stringc"), Arrays.toString(objects));
        Preconditions.checkState(objects[3].equals("stringd"), Arrays.toString(objects));
        Preconditions.checkState(objects[4].equals("stringe"), Arrays.toString(objects));
        Preconditions.checkState(objects[5].equals("stringf"), Arrays.toString(objects));
        Preconditions.checkState(objects[6].equals("stringg"), Arrays.toString(objects));
    }

    @Benchmark
    public void fallback() {
        ArgumentParserResult parse = FALLBACK_ARGUMENT_PARSER.parse(CONTEXT, COMMAND_MATCH, INPUT);
        Preconditions.checkState(parse.success(), parse);
        Object[] objects = parse.parsedArguments();
        Preconditions.checkState(objects[0].equals("stringa"), Arrays.toString(objects));
        Preconditions.checkState(objects[1].equals("stringb"), Arrays.toString(objects));
        Preconditions.checkState(objects[2].equals("stringc"), Arrays.toString(objects));
        Preconditions.checkState(objects[3].equals("stringd"), Arrays.toString(objects));
        Preconditions.checkState(objects[4].equals("stringe"), Arrays.toString(objects));
        Preconditions.checkState(objects[5].equals("stringf"), Arrays.toString(objects));
        Preconditions.checkState(objects[6].equals("stringg"), Arrays.toString(objects));
    }
}
