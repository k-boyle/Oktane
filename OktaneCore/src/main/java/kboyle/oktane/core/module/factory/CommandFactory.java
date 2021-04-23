package kboyle.oktane.core.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.exceptions.FailedToInstantiateCommandCallback;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandParameter;
import kboyle.oktane.core.module.CommandUtils;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.annotations.*;
import kboyle.oktane.core.module.callback.*;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.results.command.CommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static kboyle.oktane.core.module.CommandUtils.getCallbackFunction;
import static kboyle.oktane.core.module.CommandUtils.getModuleFactory;

public class CommandFactory<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Class<?>, TypeParser<?>> typeParserByClass;
    private final Class<MODULE> moduleClass;
    private final boolean singleton;
    private final Object moduleLock;
    private final BeanProvider beanProvider;

    public CommandFactory(
            Map<Class<?>, TypeParser<?>> typeParserByClass,
            Class<MODULE> moduleClass,
            boolean singleton,
            Object moduleLock,
            BeanProvider beanProvider) {
        this.typeParserByClass = typeParserByClass;
        this.moduleClass = moduleClass;
        this.singleton = singleton;
        this.moduleLock = moduleLock;
        this.beanProvider = beanProvider;
    }

    public Command.Builder createCommand(Aliases moduleGroups, Method method) {
        if (!isValidCommandSignature(method)) {
            return null;
        }

        Aliases commandAliases = method.getAnnotation(Aliases.class);
        Preconditions.checkState(
            isValidAliases(moduleGroups, commandAliases),
            "A command must have aliases if the module has no groups"
        );

        boolean commandSynchronised = method.getAnnotation(Synchronised.class) != null;

        Command.Builder commandBuilder = Command.builder()
            .withName(method.getName())
            .withSynchronised(commandSynchronised)
            .withCallback(getCallback(method, commandSynchronised));

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

        CommandUtils.createPreconditions(method).forEach(commandBuilder::withPrecondition);

        CommandParameterFactory parameterFactory = new CommandParameterFactory(typeParserByClass, method);
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            CommandParameter.Builder commandParameter = parameterFactory.createParameter(parameter);
            commandBuilder.withParameter(commandParameter);
        }

        return commandBuilder;
    }

    private static boolean isValidCommandSignature(Method method) {
        Type returnType = method.getGenericReturnType();
        return !isStatic(method.getModifiers())
            && isPublic(method.getModifiers())
            && isCorrectReturnType(returnType);
    }

    private static boolean isCorrectReturnType(Type returnType) {
        return returnType.equals(CommandResult.class)
            || returnType instanceof ParameterizedType parameterizedType
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

    @SuppressWarnings("unchecked")
    private CommandCallback getCallback(Method method, boolean commandSynchronised) {
        String generatedClassPath = getGenerateClassName(method);
        AnnotatedCommandCallback<CONTEXT, MODULE> callback;
        try {
            Class<?> commandClass = Class.forName(generatedClassPath);
            Constructor<?> constructor = commandClass.getConstructors()[0];
            callback = (AnnotatedCommandCallback<CONTEXT, MODULE>) constructor.newInstance();
        } catch (ClassNotFoundException e) {
            logger.warn(
                "Failed to find a class for method {} using {}",
                method,
                generatedClassPath
            );

            callback = new ReflectedCommandCallback<>(getModuleFactory(moduleClass), getCallbackFunction(method));
        } catch (Exception ex) {
            throw new FailedToInstantiateCommandCallback(ex);
        }

        if (singleton) {
            callback = new SingletonCommandCallback<>(callback, beanProvider.getBean(moduleClass));
        }

        if (commandSynchronised) {
            callback = new SynchronisedCommandCallback<>(callback);
        }

        if (moduleLock != null) {
            callback = new GloballySynchronisedCommandCallback<>(callback, moduleLock);
        }

        return callback;
    }

    private String getGenerateClassName(Method method) {
        String classPath = unwrap(moduleClass);
        String parameterNameString = Arrays.stream(method.getParameters())
            .map(parameter ->
                parameter.getParameterizedType().getTypeName()
                    .replace(".", "0")
                    .replace("<", "$$")
                    .replace(">", "$$")
            )
            .collect(Collectors.joining("_"));

        return moduleClass.getPackageName() + "." + String.join("$", classPath, method.getName(), parameterNameString);
    }

    private String unwrap(Class<?> cl) {
        Class<?> enclosing = cl.getEnclosingClass();

        if (enclosing == null) {
            return cl.getSimpleName();
        }

        return unwrap(enclosing) + "$$" + cl.getSimpleName();
    }
}
