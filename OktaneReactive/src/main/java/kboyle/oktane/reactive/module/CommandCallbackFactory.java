package kboyle.oktane.reactive.module;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import com.google.common.reflect.TypeToken;
import kboyle.oktane.reactive.BeanProvider;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.generation.AccessModifier;
import kboyle.oktane.reactive.generation.ClassGenerator;
import kboyle.oktane.reactive.generation.MethodGenerator;
import kboyle.oktane.reactive.generation.RuntimeClassFactory;
import kboyle.oktane.reactive.results.command.CommandExceptionResult;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static kboyle.oktane.reactive.ReflectionUtil.unwrap;

public class CommandCallbackFactory {
    private static final String CAST_TEMPLATE = "(%s)%s[%d]";

    @SuppressWarnings("unchecked")
    public <T extends CommandContext> CommandCallback createCommandCallback(
            Class<? extends CommandModuleBase<T>> moduleClazz,
            boolean singleton,
            Object moduleLock,
            boolean commandSynchronised,
            Method method,
            BeanProvider beanProvider) {
        Constructor<?>[] constructors = moduleClazz.getConstructors();
        Preconditions.checkState(constructors.length == 1, "There must be only 1 public constructor");

        ParameterizedType moduleTypeParameterized = unwrap(moduleClazz);
        Class<T> concreteCommandContextClazz = (Class<T>) moduleTypeParameterized.getActualTypeArguments()[0];

        String generatedName = moduleClazz.getSimpleName() + method.getName() + System.nanoTime();
        ClassGenerator classGenerator = new ClassGenerator()
            .withPackage(getClass().getPackageName())
            .withAccess(AccessModifier.PUBLIC)
            .withName(generatedName)
            .withImplementation(CommandCallback.class);

        Constructor<?> constructor = constructors[0];

        Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
        Set<Type> additionalImports = new HashSet<>(Arrays.asList(constructorParameterTypes));
        additionalImports.addAll(Arrays.asList(method.getParameterTypes()));
        additionalImports.add(CommandExceptionResult.class);
        additionalImports.add(moduleClazz);
        additionalImports.add(concreteCommandContextClazz);
        additionalImports.add(Mono.class);

        CommandModuleBase<T> module = getModule(singleton, moduleClazz, beanProvider);
        List<Object> constructorParameters = new ArrayList<>();

        MethodGenerator methodGenerator = new MethodGenerator()
            .withAccess(AccessModifier.PUBLIC)
            .withName("execute")
            .withReturn(new TypeToken<Mono<CommandResult>>() { }.getType())
            .withSynchronised(commandSynchronised)
            .withParameter(CommandContext.class, "context")
            .withParameter(Object[].class, "beans")
            .withParameter(Object[].class, "parameters");

        StringBuilder bodyBuilder = new StringBuilder();

        if (moduleLock != null) {
            classGenerator.withField(Object.class, "lock", AccessModifier.PRIVATE, true);
            constructorParameters.add(moduleLock);
            bodyBuilder.append("synchronized(lock) {");
        }

        bodyBuilder.append(moduleClazz.getSimpleName())
            .append(" module = ");

        if (module != null) {
            classGenerator.withField(moduleClazz, "module", AccessModifier.PRIVATE, true);
            constructorParameters.add(module);
            bodyBuilder.append("this.module;");
        } else {
            bodyBuilder.append("new ")
                .append(moduleClazz.getSimpleName())
                .append("(")
                .append(deconstruct(constructorParameterTypes, "beans"))
                .append(");");
        }

        // todo setCommand
        bodyBuilder.append("module.setContext((")
            .append(concreteCommandContextClazz.getSimpleName())
            .append(") context);")
            .append("try {")
            .append("return module.")
            .append(method.getName())
            .append("(")
            .append(deconstruct(method.getParameterTypes(), "parameters"))
            .append(");")
            .append("} catch (Exception ex) {")
            .append("return Mono.just(new CommandExceptionResult(module.command(), ex));")
            .append("}");

        if (moduleLock != null) {
            bodyBuilder.append("}");
        }

        classGenerator.withMethod(methodGenerator.withBody(additionalImports, bodyBuilder.toString()));

        String generatedCode = classGenerator.generate();

        return RuntimeClassFactory.compile(
            CommandCallback.class,
            generatedName,
            generatedCode,
            constructorParameters.toArray()
        );
    }

    private static <T extends CommandContext> CommandModuleBase<T> getModule(
            boolean singleton,
            Class<? extends CommandModuleBase<T>> moduleClazz,
            BeanProvider beanProvider) {
        if (!singleton) {
            return null;
        }

        return Preconditions.checkNotNull(
            beanProvider.getBean(moduleClazz),
            "Singleton module must be supplied by the beanProvider"
        );
    }

    private String deconstruct(Class<?>[] constructorParameterTypes, String arrayName) {
        return Streams.zip(
            Arrays.stream(constructorParameterTypes),
            IntStream.range(0, constructorParameterTypes.length).boxed(),
            (parameter, index) -> String.format(CAST_TEMPLATE, parameter.getSimpleName(), arrayName, index)
        )
            .collect(Collectors.joining(", "));
    }
}
