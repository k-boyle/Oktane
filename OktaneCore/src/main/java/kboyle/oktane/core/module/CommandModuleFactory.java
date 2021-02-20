package kboyle.oktane.core.module;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.annotations.CommandDescription;
import kboyle.oktane.core.module.annotations.ModuleDescription;
import kboyle.oktane.core.module.annotations.ParameterDescription;
import kboyle.oktane.core.results.command.CommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static java.lang.reflect.Modifier.isStatic;

public final class CommandModuleFactory {
    private static final Logger logger = LoggerFactory.getLogger(CommandModuleFactory.class);

    private static final String SPACE = " ";

    private CommandModuleFactory() {
    }

    // todo this has gotten very messy
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
        boolean moduleSynchronised = false;
        Object moduleLock = null;
        if (moduleDescriptionAnnotation != null) {
            singleton = moduleDescriptionAnnotation.singleton();
            moduleBuilder.withSingleton(singleton);

            moduleSynchronised = moduleDescriptionAnnotation.synchronised();
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

            Class<? extends Precondition>[] preconditionClazzes = moduleDescriptionAnnotation.preconditions();
            for (Class<? extends Precondition> preconditionClazz : preconditionClazzes) {
                Precondition precondition = Preconditions.checkNotNull(
                    beanProvider.getBean(preconditionClazz),
                    "A precondition of type %s must be added to the bean provider",
                    preconditionClazz
                );
                moduleBuilder.withPrecondition(precondition);
            }
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

            boolean commandSynchronised = commandDescriptionAnnotation.synchronised();

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

            Class<? extends Precondition>[] preconditionClazzes = commandDescriptionAnnotation.preconditions();
            for (Class<? extends Precondition> preconditionClazz : preconditionClazzes) {
                Precondition precondition = Preconditions.checkNotNull(
                    beanProvider.getBean(preconditionClazz),
                    "A precondition of type %s must be added to the bean provider",
                    preconditionClazz
                );
                commandBuilder.withPrecondition(precondition);
            }

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
}
