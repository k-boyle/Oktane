package kboyle.oktane.reactive.module;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import kboyle.oktane.reactive.BeanProvider;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.exceptions.FailedToInstantiatePreconditionException;
import kboyle.oktane.reactive.exceptions.InvalidConstructorException;
import kboyle.oktane.reactive.module.annotations.*;
import kboyle.oktane.reactive.parsers.EnumReactiveTypeParser;
import kboyle.oktane.reactive.parsers.ReactiveTypeParser;
import kboyle.oktane.reactive.results.command.CommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

public class CommandModuleFactory {
    private final Logger logger = LoggerFactory.getLogger(CommandModuleFactory.class);

    private final BeanProvider beanProvider;
    private final Map<Class<?>, ReactiveTypeParser<?>> typeParserByClass;
    private final CommandCallbackFactory callbackFactory;

    public CommandModuleFactory(BeanProvider beanProvider, Map<Class<?>, ReactiveTypeParser<?>> typeParserByClass) {
        this.beanProvider = beanProvider;
        this.typeParserByClass = typeParserByClass;
        this.callbackFactory = new CommandCallbackFactory();
    }

    public <S extends CommandContext, T extends ReactiveModuleBase<S>> ReactiveModule create(Class<T> moduleClazz) {
        Preconditions.checkState(!Modifier.isAbstract(moduleClazz.getModifiers()), "A module cannot be abstract");

        logger.trace("Creating module from {}", moduleClazz.getSimpleName());

        ReactiveModule.Builder moduleBuilder = ReactiveModule.builder()
            .withName(moduleClazz.getSimpleName());

        Constructor<?>[] constructors = moduleClazz.getConstructors();
        Preconditions.checkState(constructors.length == 1, "There must be only 1 public constructor");

        Constructor<?> constructor = constructors[0];

        Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
        for (Class<?> parameterType : constructorParameterTypes) {
            moduleBuilder.withBean(parameterType);
        }

        Aliases moduleGroups = moduleClazz.getAnnotation(Aliases.class);
        if (moduleGroups != null) {
            for (String group : moduleGroups.value()) {
                moduleBuilder.withGroup(group);
            }
        }

        boolean singleton = moduleClazz.getAnnotation(Singleton.class) != null;
        boolean moduleSynchronised = moduleClazz.getAnnotation(Synchronised.class) != null;
        moduleBuilder.withSingleton(singleton);
        moduleBuilder.withSynchronised(moduleSynchronised);
        Object moduleLock = moduleSynchronised ? new Object() : null;

        Description moduleDescription = moduleClazz.getAnnotation(Description.class);
        if (moduleDescription != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(moduleDescription.value()), "A module description must be non-empty.");
            moduleBuilder.withDescription(moduleDescription.value());
        }

        createPreconditions(moduleClazz).forEach(moduleBuilder::withPrecondition);

        Name moduleName = moduleClazz.getAnnotation(Name.class);
        if (moduleName != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(moduleName.value()), "A module name must be non-empty.");
            moduleBuilder.withName(moduleName.value());
        }

        Method[] methods = moduleClazz.getMethods();
        for (Method method : methods) {
            if (!isValidCommandSignature(method)) {
                continue;
            }

            logger.trace("Creating command from {}", method.getName());

            moduleBuilder.withCommand(
                createCommand(
                    moduleClazz,
                    callbackFactory,
                    moduleGroups,
                    singleton,
                    moduleLock,
                    method
                )
            );
        }

        logger.trace("Created module {}", moduleClazz.getSimpleName());

        return moduleBuilder.build();
    }

    private <S extends CommandContext, T extends ReactiveModuleBase<S>> ReactiveCommand.Builder createCommand(
            Class<T> moduleClazz,
            CommandCallbackFactory callbackFactory,
            Aliases moduleGroups,
            boolean singleton,
            Object moduleLock,
            Method method) {
        Aliases commandAliases = method.getAnnotation(Aliases.class);
        Preconditions.checkState(
            isValidAliases(moduleGroups, commandAliases),
            "A command must have aliases if the module has no groups"
        );

        boolean commandSynchronised = method.getAnnotation(Synchronised.class) != null;

        ReactiveCommand.Builder commandBuilder = ReactiveCommand.builder()
            .withName(method.getName())
            .withSynchronised(commandSynchronised)
            .withCallback(callbackFactory.createCommandCallback(
                moduleClazz,
                singleton,
                moduleLock,
                commandSynchronised,
                method,
                beanProvider
            ));

        if (commandAliases != null) {
            for (String alias : commandAliases.value()) {
                commandBuilder.withAlias(alias);
            }
        }

        Name commandName = method.getAnnotation(Name.class);
        if (commandName != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(commandName.value()), "A command name must be non-empty.");
            commandBuilder.withName(commandName.value());
        }

        Description commandDescription = method.getAnnotation(Description.class);
        if (commandDescription != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(commandDescription.value()), "A command description must be non-empty.");
            commandBuilder.withDescription(commandDescription.value());
        }

        Priority priority = method.getAnnotation(Priority.class);
        if (priority != null) {
            commandBuilder.withPriority(priority.value());
        }

        createPreconditions(method).forEach(commandBuilder::withPrecondition);

        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            ReactiveCommandParameter.Builder commandParameter = createParameter(method, parameter);
            commandBuilder.withParameter(commandParameter);
        }

        return commandBuilder;
    }

    private ReactiveCommandParameter.Builder createParameter(Method method, Parameter parameter) {
        Class<?> parameterType = parameter.getType();

        ReactiveTypeParser<?> parser = typeParserByClass.get(parameterType);
        if (parser == null && parameterType.isEnum()) {
            parser = typeParserByClass.computeIfAbsent(parameterType, type -> new EnumReactiveTypeParser(type));
        }

        ReactiveCommandParameter.Builder parameterBuilder = ReactiveCommandParameter.builder()
            .withType(parameterType)
            .withName(parameter.getName())
            .withRemainder(parameter.getAnnotation(Remainder.class) != null)
            .withParser(parser);

        Description parameterDescription = method.getAnnotation(Description.class);
        if (parameterDescription != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(parameterDescription.value()), "A parameter description must be non-empty.");
            parameterBuilder.withDescription(parameterDescription.value());
        }

        Name parameterName = parameter.getAnnotation(Name.class);
        if (parameterName != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(parameterName.value()), "A parameter name must be non-empty.");
            parameterBuilder.withName(parameterName.value());
        }

        return parameterBuilder;
    }

    private static boolean isValidCommandSignature(Method method) {
        Type returnType = method.getGenericReturnType();
        return !isStatic(method.getModifiers())
            && isPublic(method.getModifiers())
            && returnType instanceof ParameterizedType parameterizedType
            && parameterizedType.getRawType() instanceof Class<?> rawTypeClazz
            && rawTypeClazz.isAssignableFrom(Mono.class)
            && parameterizedType.getActualTypeArguments().length == 1
            && parameterizedType.getActualTypeArguments()[0] instanceof Class<?> typeArgumentClazz
            && typeArgumentClazz.isAssignableFrom(CommandResult.class);
    }

    private static boolean isValidAliases(Aliases moduleAliases, Aliases commandAliases) {
        return commandAliases != null && commandAliases.value().length > 0
            || moduleAliases != null && moduleAliases.value().length > 0;
    }

    private static Stream<ReactivePrecondition> createPreconditions(AnnotatedElement element) {
        return Arrays.stream(element.getAnnotationsByType(Require.class))
            .map(CommandModuleFactory::initPrecondition);
    }

    private static ReactivePrecondition initPrecondition(Require requirement) {
        Class<? extends ReactivePrecondition> clazz = requirement.precondition();
        String[] arguments = requirement.arguments();
        Constructor<?> validConstructor = Arrays.stream(clazz.getConstructors())
            .filter(CommandModuleFactory::isValidConstructor)
            .reduce((single, other) -> {
                throw new InvalidConstructorException("Expected only a single constructor");
            })
            .orElseThrow(() -> new InvalidConstructorException("Expected at least one valid constructor"));
        try {
            if (arguments.length == 0) {
                return (ReactivePrecondition) validConstructor.newInstance();
            }

            return (ReactivePrecondition) validConstructor.newInstance((Object) arguments);
        } catch (Exception ex) {
            throw new FailedToInstantiatePreconditionException(clazz, ex);
        }
    }

    private static boolean isValidConstructor(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        return parameters.length == 0 || parameters.length == 1 && parameters[0].getType().equals(String[].class);
    }
}
