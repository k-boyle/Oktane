package kboyle.oktane.core.module;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.generation.AccessModifier;
import kboyle.oktane.core.generation.ClassGenerator;
import kboyle.oktane.core.generation.MethodGenerator;
import kboyle.oktane.core.generation.RuntimeClassFactory;
import kboyle.oktane.core.results.command.CommandExecutionErrorResult;
import kboyle.oktane.core.results.command.CommandResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommandCallbackFactory {
    private static final String CAST_TEMPLATE = "(%s)%s[%d]";

    public <T extends CommandContext> CommandCallback createCommandCallback(
            Class<T> concreteCommandContextClazz,
            Class<? extends CommandModuleBase<T>> moduleClazz,
            boolean singleton,
            Object moduleLock,
            boolean commandSynchronised,
            Method method,
            BeanProvider beanProvider) {
        Constructor<?>[] constructors = moduleClazz.getConstructors();
        Preconditions.checkState(constructors.length == 1, "There must be only 1 public constructor");

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
        additionalImports.add(CommandExecutionErrorResult.class);
        additionalImports.add(moduleClazz);
        additionalImports.add(concreteCommandContextClazz);

        CommandModuleBase<T> module = getModule(singleton, moduleClazz, beanProvider);
        List<Object> constructorParameters = new ArrayList<>();

        MethodGenerator methodGenerator = new MethodGenerator()
            .withAccess(AccessModifier.PUBLIC)
            .withName("execute")
            .withReturn(CommandResult.class)
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
            .append("return new CommandExecutionErrorResult(context.command(), ex);")
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
