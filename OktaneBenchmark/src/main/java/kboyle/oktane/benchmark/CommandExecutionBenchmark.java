package kboyle.oktane.benchmark;

import kboyle.oktane.core.module.callback.AnnotatedCommandCallback;
import kboyle.oktane.core.module.callback.ReflectedCommandCallback;
import kboyle.oktane.core.results.command.CommandResult;
import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static kboyle.oktane.core.module.CommandUtils.getCallbackFunction;
import static kboyle.oktane.core.module.CommandUtils.getModuleFactory;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
public class CommandExecutionBenchmark {
    private static final AnnotatedCommandCallback<BenchmarkContext, GeneratedBenchmarkModule> GENERATED_NO_PARAMETERS_CALLBACK;
    private static final AnnotatedCommandCallback<BenchmarkContext, GeneratedBenchmarkModule> GENERATED_ONE_PARAMETER_CALLBACK;
    private static final AnnotatedCommandCallback<BenchmarkContext, GeneratedBenchmarkModule> GENERATED_TWO_PARAMETERS_CALLBACK;

    private static final ReflectedCommandCallback<BenchmarkContext, GeneratedBenchmarkModule> REFLECTED_NO_PARAMETERS_CALLBACK;
    private static final ReflectedCommandCallback<BenchmarkContext, GeneratedBenchmarkModule> REFLECTED_ONE_PARAMETER_CALLBACK;
    private static final ReflectedCommandCallback<BenchmarkContext, GeneratedBenchmarkModule> REFLECTED_TWO_PARAMETERS_CALLBACK;

    private static final BenchmarkContext CONTEXT = new BenchmarkContext();
    private static final Object[] EMPTY = new Object[0];
    private static final Object[] ONE = new Object[] { "one" };
    private static final Object[] TWO = new Object[] { "one", "TWO" };

    static {
        GENERATED_NO_PARAMETERS_CALLBACK = createGeneratedCallback("kboyle.oktane.benchmark.GeneratedBenchmarkModule$noParameters$");
        GENERATED_ONE_PARAMETER_CALLBACK = createGeneratedCallback("kboyle.oktane.benchmark.GeneratedBenchmarkModule$oneParameter$java0lang0String");
        GENERATED_TWO_PARAMETERS_CALLBACK = createGeneratedCallback("kboyle.oktane.benchmark.GeneratedBenchmarkModule$twoParameters$java0lang0String_java0lang0String");

        REFLECTED_NO_PARAMETERS_CALLBACK = createReflectedCallback("noParameters");
        REFLECTED_ONE_PARAMETER_CALLBACK = createReflectedCallback("oneParameter", String.class);
        REFLECTED_TWO_PARAMETERS_CALLBACK = createReflectedCallback("twoParameters", String.class, String.class);
    }

    @SuppressWarnings("unchecked")
    private static AnnotatedCommandCallback<BenchmarkContext, GeneratedBenchmarkModule> createGeneratedCallback(String name) {
        try {
            Class<?> cl = Class.forName(name);
            return (AnnotatedCommandCallback<BenchmarkContext, GeneratedBenchmarkModule>) cl.getConstructors()[0].newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ReflectedCommandCallback<BenchmarkContext, GeneratedBenchmarkModule> createReflectedCallback(String name, Class<?>... parameterTypes) {
        try {
            Method method = GeneratedBenchmarkModule.class.getMethod(name, parameterTypes);
            return new ReflectedCommandCallback<>(getModuleFactory(GeneratedBenchmarkModule.class), getCallbackFunction(method));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Benchmark
    public Mono<CommandResult> directNoParameters() {
        GeneratedBenchmarkModule module = new GeneratedBenchmarkModule();
        module.setContext(CONTEXT);
        return module.noParameters().mono();
    }

    @Benchmark
    public Mono<CommandResult> directOneParameter() {
        GeneratedBenchmarkModule module = new GeneratedBenchmarkModule();
        module.setContext(CONTEXT);
        return module.oneParameter("one").mono();
    }

    @Benchmark
    public Mono<CommandResult> directTwoParameters() {
        GeneratedBenchmarkModule module = new GeneratedBenchmarkModule();
        module.setContext(CONTEXT);
        return module.twoParameters("one", "two").mono();
    }

    @Benchmark
    public Mono<CommandResult> generatedNoParameters() {
        return GENERATED_NO_PARAMETERS_CALLBACK.execute(CONTEXT, EMPTY, EMPTY);
    }

    @Benchmark
    public Mono<CommandResult> generatedOneParameter() {
        return GENERATED_ONE_PARAMETER_CALLBACK.execute(CONTEXT, EMPTY, ONE);
    }

    @Benchmark
    public Mono<CommandResult> generatedTwoParameters() {
        return GENERATED_TWO_PARAMETERS_CALLBACK.execute(CONTEXT, EMPTY, TWO);
    }

    @Benchmark
    public Mono<CommandResult> reflectedNoParameters() {
        return REFLECTED_NO_PARAMETERS_CALLBACK.execute(CONTEXT, EMPTY, EMPTY);
    }

    @Benchmark
    public Mono<CommandResult> reflectedOneParameter() {
        return REFLECTED_ONE_PARAMETER_CALLBACK.execute(CONTEXT, EMPTY, ONE);
    }

    @Benchmark
    public Mono<CommandResult> reflectedTwoParameters() {
        return REFLECTED_TWO_PARAMETERS_CALLBACK.execute(CONTEXT, EMPTY, TWO);
    }
}
