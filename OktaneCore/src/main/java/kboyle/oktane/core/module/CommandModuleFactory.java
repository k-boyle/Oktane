package kboyle.oktane.core.module;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.exceptions.FailedToInstantiatePreconditionException;
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

    private static final String SPACE = " ";

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

        ModuleDescription moduleDescriptionAnnotation = moduleClazz.getAnnotation(ModuleDescription.class);
        boolean singleton = false;
        boolean moduleSynchronised;
        Object moduleLock = null;
        if (moduleDescriptionAnnotation != null) {
            singleton = moduleDescriptionAnnotation.singleton() || moduleClazz.getAnnotation(Singleton.class) != null;
            moduleBuilder.withSingleton(singleton);

            moduleSynchronised = moduleDescriptionAnnotation.synchronised() || moduleClazz.getAnnotation(Synchronised.class) != null;
            moduleBuilder.withSynchronised(moduleSynchronised);

            if (moduleSynchronised) {
                moduleLock = new Object();
            }

            if (!Strings.isNullOrEmpty(moduleDescriptionAnnotation.name())) {
                moduleBuilder.withName(moduleDescriptionAnnotation.name());
            }

            if (!Strings.isNullOrEmpty(moduleDescriptionAnnotation.description())) {
                moduleBuilder.withDescription(moduleDescriptionAnnotation.description());
            }

            for (String group : moduleDescriptionAnnotation.groups()) {
               moduleBuilder.withGroup(group);
            }

            getPreconditions(moduleClazz, moduleDescriptionAnnotation, beanProvider)
                .forEach(moduleBuilder::withPrecondition);
        }

        Method[] methods = moduleClazz.getMethods();
        for (Method method : methods) {
            CommandDescription commandDescriptionAnnotation = method.getAnnotation(CommandDescription.class);
            if (commandDescriptionAnnotation == null) {
                continue;
            }

            logger.trace("Creating command from {}", method.getName());

            Preconditions.checkState(
                isValidCommandSignature(method),
                "Method %s has invalid signature",
                method
            );
            Preconditions.checkState(
                isValidAliases(moduleDescriptionAnnotation, commandDescriptionAnnotation),
                "A command must have aliases if the module has no groups"
            );

            boolean commandSynchronised = commandDescriptionAnnotation.synchronised() || method.getAnnotation(Synchronised.class) != null;

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

            for (String alias : commandDescriptionAnnotation.aliases()) {
                commandBuilder.withAliases(alias);
            }

            if (!Strings.isNullOrEmpty(commandDescriptionAnnotation.name())) {
                commandBuilder.withName(commandDescriptionAnnotation.name());
            }

            if (!Strings.isNullOrEmpty(commandDescriptionAnnotation.description())) {
                commandBuilder.withDescription(commandDescriptionAnnotation.description());
            }


            getPreconditions(method, commandDescriptionAnnotation, beanProvider)
                .forEach(commandBuilder::withPrecondition);

            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                Class<?> parameterType = parameter.getType();
                CommandParameter.Builder commandParameterBuilder = CommandParameter.builder()
                    .withType(parameterType)
                    .withName(parameter.getName());

                ParameterDescription parameterDescriptionAnnotation = parameter.getAnnotation(ParameterDescription.class);
                if (parameterDescriptionAnnotation != null) {
                    commandParameterBuilder.withDescription(parameterDescriptionAnnotation.description())
                        .withRemainder(parameterDescriptionAnnotation.remainder())
                        .withName(parameterDescriptionAnnotation.name());
                }

                commandBuilder.withParameter(commandParameterBuilder.build());
            }

            moduleBuilder.withCommand(commandBuilder);
        }

        logger.trace("Created module {}", moduleClazz.getSimpleName());

        return moduleBuilder.build();
    }

    private static boolean isValidCommandSignature(Method method) {
        return !isStatic(method.getModifiers()) && method.getReturnType().equals(CommandResult.class);
    }

    private static boolean isValidAliases(
            ModuleDescription moduleDescriptionAnnotation,
            CommandDescription commandDescriptionAnnotation) {
        for (String alias : commandDescriptionAnnotation.aliases()) {
            Preconditions.checkState(!alias.contains(SPACE), "Alias %s contains a space", alias);
        }

        return commandDescriptionAnnotation.aliases().length > 0
            || moduleDescriptionAnnotation != null
            && moduleDescriptionAnnotation.groups().length > 0;
    }

    private static Stream<Precondition> getPreconditions(
            Class<?> clazz,
            ModuleDescription description,
            BeanProvider beanProvider) {
        return getPreconditions(description.preconditions(), clazz.getAnnotationsByType(Requires.class), beanProvider);
    }

    private static Stream<Precondition> getPreconditions(
            Method method,
            CommandDescription description,
            BeanProvider beanProvider) {
        return getPreconditions(description.preconditions(), method.getAnnotationsByType(Requires.class), beanProvider);
    }

    private static Stream<Precondition> getPreconditions(
            Class<? extends Precondition>[] preconditions,
            Requires[] requires,
            BeanProvider beanProvider) {
        Stream<Precondition> oldStyle = Arrays.stream(preconditions)
            .map(precondition -> Preconditions.checkNotNull(
                beanProvider.getBean(precondition),
                "A precondition of type %s must be added to the bean provider",
                precondition
            ));

        Stream<Precondition> newStyle = Arrays.stream(requires)
            .map(CommandModuleFactory::initPrecondition);

        return Streams.concat(oldStyle, newStyle);
    }

    private static Precondition initPrecondition(Requires requirement) {
        Class<? extends Precondition> clazz = requirement.precondition();
        String[] arguments = requirement.arguments();
        Constructor<?> validConstructor = Arrays.stream(clazz.getConstructors())
            .filter(constructor -> constructor.getParameters().length == arguments.length)
            .reduce((single, other) -> {
                throw new RuntimeException("add a proper exception");
            })
            .orElseThrow(() -> new RuntimeException("exception fool"));
        try {
            return (Precondition) validConstructor.newInstance((Object[]) arguments);
        } catch (Exception ex) {
            throw new FailedToInstantiatePreconditionException(clazz, ex);
        }
    }
}
