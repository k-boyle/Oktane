package kboyle.oktane.benchmark;

import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandCallback;
import kboyle.oktane.core.module.CommandModuleFactory;
import kboyle.oktane.core.module.Module;
import kboyle.oktane.core.results.command.CommandResult;
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
public class CommandExecutionBenchmark {
    private final Module module = CommandModuleFactory.create(BenchmarkCommandContext.class, BenchmarkModule.class, BeanProvider.get());
    private final Map<String, Command> commands = module.commands().stream()
        .collect(Collectors.toMap(Command::name, Function.identity()));

    private final CommandCallback c = commands.get("f").commandCallback();
    private final CommandCallback b = commands.get("b").commandCallback();
    private final CommandCallback a = commands.get("a").commandCallback();

    private final Object[] empty = new Object[0];
    private final Object[] one = new Object[] { "abc" };
    private final Object[] five = new Object[] { "a", "b", "c", "d", "e" };
    private final BenchmarkCommandContext context = new BenchmarkCommandContext();

    @Benchmark
    public CommandResult commandNoParameters() {
        return a.execute(context, empty, empty);
    }

    @Benchmark
    public CommandResult commandOneParameter() {
        return b.execute(context, empty, one);
    }

    @Benchmark
    public CommandResult commandFiveParameters() {
        return c.execute(context, empty, five);
    }


    @Benchmark
    public CommandResult directCommandNoParameters() {
        BenchmarkModule benchmarkModule = new BenchmarkModule();
        benchmarkModule.setContext(context);
        return benchmarkModule.a();
    }

    @Benchmark
    public CommandResult directCommandOneParameter() {
        BenchmarkModule benchmarkModule = new BenchmarkModule();
        benchmarkModule.setContext(context);
        return benchmarkModule.b("abc");
    }

    @Benchmark
    public CommandResult directCommandFiveParameters() {
        BenchmarkModule benchmarkModule = new BenchmarkModule();
        benchmarkModule.setContext(context);
        return benchmarkModule.f("a", "b", "c", "d", "e");
    }
}
