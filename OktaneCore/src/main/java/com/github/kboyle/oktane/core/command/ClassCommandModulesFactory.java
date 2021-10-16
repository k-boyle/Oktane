package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.annotation.*;
import com.github.kboyle.oktane.core.execution.*;
import com.github.kboyle.oktane.core.parsing.TypeParser;
import com.github.kboyle.oktane.core.parsing.TypeParserProvider;
import com.github.kboyle.oktane.core.precondition.Precondition;
import com.github.kboyle.oktane.core.result.command.CommandResult;
import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.kboyle.oktane.core.Utilities.Functions.supplyNull;
import static com.github.kboyle.oktane.core.Utilities.Functions.zero;
import static com.github.kboyle.oktane.core.Utilities.Streams.single;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

// todo this bad bad bad bad bad
class ClassCommandModulesFactory<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> implements CommandModulesFactory {
    private final List<Class<MODULE>> moduleClasses;
    private final Consumer<? super CommandModule.Builder> commandModuleBuilderConsumer;

    ClassCommandModulesFactory(List<Class<MODULE>> moduleClasses, Consumer<? super CommandModule.Builder> commandModuleBuilderConsumer) {
        this.moduleClasses = Preconditions.checkNotNull(moduleClasses, "moduleClasses cannot be null");
        this.commandModuleBuilderConsumer = Preconditions.checkNotNull(commandModuleBuilderConsumer, "commandModuleBuilderConsumer cannot be null");
    }

    @Override
    public Stream<CommandModule> createModules(TypeParserProvider typeParserProvider) {
        return moduleClasses.stream()
            .map(cl -> createModule(cl, typeParserProvider))
            .map(builder -> builder.build(null));
    }

    private CommandModule.Builder createModule(Class<MODULE> moduleClass, TypeParserProvider typeParserProvider) {
        Preconditions.checkNotNull(moduleClass, "moduleClass cannot be null");

        var moduleBuilder = CommandModule.builder();
        moduleBuilder.name(name(moduleClass, moduleClass::getSimpleName));

        for (var dependency : dependencies(moduleClass)) {
            moduleBuilder.dependency(dependency);
        }

        for (var annotation : annotations(moduleClass)) {
            moduleBuilder.annotation(annotation);
        }

        for (var group : aliases(moduleClass)) {
            moduleBuilder.group(group);
        }

        var description = description(moduleClass, supplyNull());
        if (description != null) {
            moduleBuilder.description(description);
        }

        var synchronised = synchronised(moduleClass);
        var singleton = singleton(moduleClass);
        moduleBuilder.synchronised(synchronised);
        moduleBuilder.singleton(singleton);

        preconditions(moduleClass).forEach(moduleBuilder::precondition);
        commands(moduleClass, typeParserProvider, synchronised ? new Object() : null, singleton).forEach(moduleBuilder::command);
        children(moduleClass, typeParserProvider).forEach(moduleBuilder::child);

        commandModuleBuilderConsumer.accept(moduleBuilder);
        return moduleBuilder;
    }

    @SuppressWarnings("unchecked")
    private Stream<CommandModule.Builder> children(Class<MODULE> moduleClass, TypeParserProvider typeParserProvider) {
        return Arrays.stream(moduleClass.getDeclaredClasses())
            .filter(cl -> isStatic(cl.getModifiers()) && cl.getSuperclass() == moduleClass)
            .map(cl -> createModule((Class<MODULE>) cl, typeParserProvider));
    }

    private Stream<Command.Builder> commands(
            Class<MODULE> moduleClass,
            TypeParserProvider typeParserProvider,
            Object moduleLock,
            boolean singleton) {

        return Arrays.stream(moduleClass.getDeclaredMethods())
            .filter(this::validCommandSignature)
            .map(method -> createCommand(moduleClass, typeParserProvider, method, moduleLock, singleton));
    }

    @SuppressWarnings("UnstableApiUsage")
    private Stream<CommandParameter.Builder<?>> parameters(Method method, TypeParserProvider typeParserProvider) {
        var parameters = method.getParameters();
        return Streams.zip(Arrays.stream(parameters), IntStream.range(0, parameters.length).boxed(), (parameter, index) -> {
            // todo or greedy
            var varargs = varargs(method, parameters, index);
            var greedy = greedy(parameter);
            if (varargs || greedy) {
                return createParameter(parameter, parameter.getType().componentType(), typeParserProvider, varargs, greedy);
            }

            return createParameter(parameter, parameter.getType(), typeParserProvider, false, false);
        });
    }

    private String name(AnnotatedElement element, Supplier<String> or) {
        var name = element.getDeclaredAnnotation(Name.class);
        return name == null
            ? or.get()
            : name.value();
    }

    private Class<?>[] dependencies(Class<MODULE> moduleClass) {
        return single(Arrays.stream(moduleClass.getDeclaredConstructors()))
            .getParameterTypes();
    }

    private Annotation[] annotations(AnnotatedElement element) {
        return element.getDeclaredAnnotations();
    }

    private String[] aliases(AnnotatedElement element) {
        var aliases = element.getDeclaredAnnotation(Aliases.class);
        return aliases == null
            ? new String[0]
            : aliases.value();
    }

    private String description(AnnotatedElement element, Supplier<String> or) {
        var description = element.getDeclaredAnnotation(Description.class);
        return description == null
            ? or.get()
            : description.value();
    }

    private boolean singleton(Class<MODULE> moduleClass) {
        return moduleClass.isAnnotationPresent(Singleton.class);
    }

    private boolean synchronised(AnnotatedElement element) {
        return element.isAnnotationPresent(Synchronised.class);
    }

    // todo
    private Stream<Precondition> preconditions(AnnotatedElement element) {
        return Stream.of();
    }

    private Command.Builder createCommand(
            Class<MODULE> moduleClass,
            TypeParserProvider typeParserProvider,
            Method method,
            Object moduleLock,
            boolean singleton) {

        var commandBuilder = Command.builder();
        commandBuilder.name(name(method, method::getName));

        var description = description(method, supplyNull());
        if (description != null) {
            commandBuilder.description(description);
        }

        for (var annotation : annotations(method)) {
            commandBuilder.annotation(annotation);
        }

        for (var alias : aliases(method)) {
            commandBuilder.alias(alias);
        }

        var synchronised = synchronised(method);
        commandBuilder.synchronised(synchronised)
            .priority(priority(method, zero()));

        preconditions(method).forEach(commandBuilder::precondition);

        var callback = callback(moduleClass, method, moduleLock, singleton, synchronised);

        parameters(method, typeParserProvider).forEach(commandBuilder::parameter);

        commandBuilder.callback(callback);
        return commandBuilder;
    }

    private boolean validCommandSignature(Method method) {
        var returnType = method.getGenericReturnType();
        return !isStatic(method.getModifiers())
            && isPublic(method.getModifiers())
            && correctReturnType(returnType);
    }

    private static boolean correctReturnType(Type returnType) {
        return returnType.equals(CommandResult.class);
    }

    private int priority(Method method, IntSupplier or) {
        var priority = method.getDeclaredAnnotation(Priority.class);
        return priority == null
            ? or.getAsInt()
            : priority.value();
    }

    private AbstractCommandCallback<CONTEXT, MODULE> callback(
            Class<MODULE> moduleClass,
            Method method,
            Object moduleLock,
            boolean singleton,
            boolean synchronised) {

        var callback = AbstractCommandCallback.reflection(moduleClass, method);
        if (moduleLock != null) {
            callback = callback.synchronised(moduleLock);
        }

        if (singleton) {
            callback = callback.singleton(moduleClass);
        }

        if (synchronised) {
            callback = callback.synchronised();
        }

        return callback;
    }

    private <T> CommandParameter.Builder<T> createParameter(
            Parameter parameter,
            Class<T> parameterClass,
            TypeParserProvider typeParserProvider,
            boolean varargs,
            boolean greedy) {

        var parameterBuilder = CommandParameter.<T>builder()
            .originalParameter(parameter)
            .type(parameterClass)
            .name(name(parameter, parameter::getName))
            .remainder(remainder(parameter))
            .varargs(varargs)
            .greedy(greedy)
            .optional(optional(parameter))
            .typeParser(typeParser(parameter, parameterClass, typeParserProvider));

        for (var annotation : annotations(parameter)) {
            parameterBuilder.annotation(annotation);
        }

        var description = description(parameter, supplyNull());
        if (description != null) {
            parameterBuilder.description(description);
        }

        var defaultString = defaultString(parameter);
        if (defaultString != null) {
            parameterBuilder.defaultString(defaultString);
        }

        return parameterBuilder;
    }

    private boolean remainder(Parameter parameter) {
        return parameter.isAnnotationPresent(Remainder.class);
    }

    private boolean optional(Parameter parameter) {
        return parameter.isAnnotationPresent(Optional.class) || parameter.isAnnotationPresent(Default.class);
    }

    private String defaultString(Parameter parameter) {
        var defaultString = parameter.getDeclaredAnnotation(Default.class);
        return defaultString == null
            ? null
            : defaultString.value();
    }

    private boolean varargs(Method method, Parameter[] parameters, int index) {
        return method.isVarArgs() && index == parameters.length - 1;
    }

    private boolean greedy(Parameter parameter) {
        var greedyAnnotation = parameter.isAnnotationPresent(Greedy.class);
        if (greedyAnnotation) {
            Preconditions.checkState(parameter.getType().isArray(), "A greedy parameter must be an array");
            Preconditions.checkState(parameter.getType().componentType() != String.class, "A greedy parameter can't be an array of strings");
        }

        return greedyAnnotation;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> TypeParser<T> typeParser(Parameter parameter, Class<T> parameterClass, TypeParserProvider typeParserProvider) {
        var override = parameter.getDeclaredAnnotation(OverrideTypeParser.class);
        if (override == null) {
            if (parameterClass.isEnum()) {
                return (TypeParser<T>) typeParserProvider.getEnum((Class<Enum>) parameterClass);
            }

            return Preconditions.checkNotNull(typeParserProvider.get(parameterClass), "Missing type parser for type %s", parameterClass);
        }

        var typeParser = Preconditions.checkNotNull(
            typeParserProvider.getOverride(override.value()),
            "Missing type parser of type %s",
            override.value()
        );

        Preconditions.checkState(typeParser.targetType() == parameterClass, "Type parser of type %s, expected %s", typeParser.targetType(), parameterClass);
        return (TypeParser<T>) typeParser;
    }
}
