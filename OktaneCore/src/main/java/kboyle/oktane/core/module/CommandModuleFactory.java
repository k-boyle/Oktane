package kboyle.oktane.core.module;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.exceptions.FailedToInstantiatePreconditionException;
import kboyle.oktane.core.exceptions.InvalidConstructorException;
import kboyle.oktane.core.module.annotations.*;
import kboyle.oktane.core.results.command.CommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isStatic;

public final class CommandModuleFactory {
    private static final Logger logger = LoggerFactory.getLogger(CommandModuleFactory.class);

    private CommandModuleFactory() {
    }

    public static <S extends CommandContext, T extends CommandModuleBase<S>> Module create(Class<T> moduleClazz, BeanProvider beanProvider) {
        logger.trace("Creating module from {}", moduleClazz.getSimpleName());

        CommandCallbackFactory callbackFactory = new CommandCallbackFactory();

        Module.Builder moduleBuilder = Module.builder()
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

            createCommand(
                moduleClazz,
                beanProvider,
                callbackFactory,
                moduleBuilder,
                moduleGroups,
                singleton,
                moduleLock,
                method
            );
        }

        logger.trace("Created module {}", moduleClazz.getSimpleName());

        return moduleBuilder.build();
    }

    private static <S extends CommandContext, T extends CommandModuleBase<S>> void createCommand(
            Class<T> moduleClazz,
            BeanProvider beanProvider,
            CommandCallbackFactory callbackFactory,
            Module.Builder moduleBuilder,
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

        Command.Builder commandBuilder = Command.builder()
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

        createPreconditions(method).forEach(commandBuilder::withPrecondition);

        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            createParameter(method, commandBuilder, parameter);
        }

        moduleBuilder.withCommand(commandBuilder);
    }

    private static void createParameter(Method method, Command.Builder commandBuilder, Parameter parameter) {
        Class<?> parameterType = parameter.getType();
        CommandParameter.Builder parameterBuilder = CommandParameter.builder()
            .withType(parameterType)
            .withName(parameter.getName())
            .withRemainder(parameter.getAnnotation(Remainder.class) != null);

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

        commandBuilder.withParameter(parameterBuilder.build());
    }

    private static boolean isValidCommandSignature(Method method) {
        return !isStatic(method.getModifiers())
            && method.getReturnType().equals(CommandResult.class)
            && method.getAnnotation(Disabled.class) == null;
    }

    private static boolean isValidAliases(Aliases moduleAliases, Aliases commandAliases) {
        return commandAliases != null && commandAliases.value().length > 0
            || moduleAliases != null && moduleAliases.value().length > 0;
    }

    private static Stream<Precondition> createPreconditions(Class<?> clazz) {
        return createPreconditions(clazz.getAnnotationsByType(Require.class));
    }

    private static Stream<Precondition> createPreconditions(Method method) {
        return createPreconditions(method.getAnnotationsByType(Require.class));
    }

    private static Stream<Precondition> createPreconditions(Require[] requires) {
        return Arrays.stream(requires).map(CommandModuleFactory::initPrecondition);
    }

    private static Precondition initPrecondition(Require requirement) {
        Class<? extends Precondition> clazz = requirement.precondition();
        String[] arguments = requirement.arguments();
        Constructor<?> validConstructor = Arrays.stream(clazz.getConstructors())
            .filter(CommandModuleFactory::isValidConstructor)
            .reduce((single, other) -> {
                throw new InvalidConstructorException("Expected only a single constructor");
            })
            .orElseThrow(() -> new InvalidConstructorException("Expected at least one valid constructor"));
        try {
            return (Precondition) validConstructor.newInstance((Object) arguments);
        } catch (Exception ex) {
            throw new FailedToInstantiatePreconditionException(clazz, ex);
        }
    }

    private static boolean isValidConstructor(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        return parameters.length == 0 || parameters.length == 1 && parameters[0].getType().equals(String[].class);
    }
}
