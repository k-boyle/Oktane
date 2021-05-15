package kboyle.oktane.core.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.exceptions.FailedToInstantiateCommandCallback;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.module.annotations.*;
import kboyle.oktane.core.module.callback.*;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.precondition.AnyPrecondition;
import kboyle.oktane.core.results.command.CommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static kboyle.oktane.core.module.factory.PreconditionFactory.NO_GROUP;

public class CommandFactory<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PreconditionFactoryMap preconditionFactoryMap;
    private final Map<Class<?>, TypeParser<?>> typeParserByClass;
    private final Class<MODULE> moduleClass;
    private final boolean singleton;
    private final Object moduleLock;
    private final BeanProvider beanProvider;

    public CommandFactory(
            PreconditionFactoryMap preconditionFactoryMap,
            Map<Class<?>, TypeParser<?>> typeParserByClass,
            Class<MODULE> moduleClass,
            boolean singleton,
            Object moduleLock,
            BeanProvider beanProvider) {
        this.preconditionFactoryMap = preconditionFactoryMap;
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

        var commandBuilder = Command.builder()
            .withName(method.getName());

        boolean synchronised = false;
        var preconditionsByGroup = HashMultimap.<Object, Precondition>create();
        for (var annotation : method.getAnnotations()) {
            if (annotation instanceof Aliases aliases) {
                Preconditions.checkState(
                    isValidAliases(moduleGroups, aliases),
                    "A command must have aliases if the module has no groups"
                );

                for (var alias : aliases.value()) {
                    commandBuilder.withAlias(alias);
                }
            } else if (annotation instanceof Synchronised) {
                commandBuilder.synchronised();
                synchronised = true;
            } else if (annotation instanceof Name name) {
                Preconditions.checkState(!Strings.isNullOrEmpty(name.value()), "A command name must be non-empty.");
                commandBuilder.withName(name.value());
            } else if (annotation instanceof Description description) {
                Preconditions.checkState(!Strings.isNullOrEmpty(description.value()), "A command description must be non-empty.");
                commandBuilder.withDescription(description.value());
            } else if (annotation instanceof Priority priority) {
                commandBuilder.withPriority(priority.value());
            } else {
                preconditionFactoryMap.handle(annotation, preconditionsByGroup::put);
            }
        }

        preconditionsByGroup.removeAll(NO_GROUP).forEach(commandBuilder::withPrecondition);
        preconditionsByGroup.asMap().values().stream()
            .map(preconditionCollection -> new AnyPrecondition(ImmutableList.copyOf(preconditionCollection)))
            .forEach(commandBuilder::withPrecondition);

        commandBuilder.withCallback(getCallback(method, synchronised));

        var parameterFactory = new CommandParameterFactory(typeParserByClass);
        var parameters = method.getParameters();
        for (var parameter : parameters) {
            var commandParameter = parameterFactory.createParameter(parameter);
            commandBuilder.withParameter(commandParameter);
        }

        return commandBuilder;
    }

    private static boolean isValidCommandSignature(Method method) {
        var returnType = method.getGenericReturnType();
        return !isStatic(method.getModifiers())
            && isPublic(method.getModifiers())
            && isCorrectReturnType(returnType);
    }

    private static boolean isCorrectReturnType(Type returnType) {
        return returnType.equals(CommandResult.class)
            || returnType instanceof ParameterizedType parameterizedType
            && parameterizedType.getRawType() instanceof Class<?> rawTypeClass
            && rawTypeClass.isAssignableFrom(Mono.class)
            && parameterizedType.getActualTypeArguments().length == 1
            && parameterizedType.getActualTypeArguments()[0] instanceof Class<?> typeArgumentClass
            && typeArgumentClass.isAssignableFrom(CommandResult.class);
    }

    private static boolean isValidAliases(Aliases moduleAliases, Aliases commandAliases) {
        return commandAliases.value().length > 0 || moduleAliases != null && moduleAliases.value().length > 0;
    }

    @SuppressWarnings("unchecked")
    private CommandCallback getCallback(Method method, boolean commandSynchronised) {
        var generatedClassPath = getGenerateClassName(method);
        AnnotatedCommandCallback<CONTEXT, MODULE> callback;
        try {
            var commandClass = Class.forName(generatedClassPath);
            var constructor = commandClass.getConstructors()[0];
            callback = (AnnotatedCommandCallback<CONTEXT, MODULE>) constructor.newInstance();
        } catch (ClassNotFoundException e) {
            logger.error(
                "Failed to find a class for method {} using {}, using reflection fallback",
                method,
                generatedClassPath
            );

            callback = ReflectedCommandFactory.createCallback(moduleClass, method);
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
        var classPath = unwrap(moduleClass);
        var parameterNameString = Arrays.stream(method.getParameters())
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
        var enclosing = cl.getEnclosingClass();

        if (enclosing == null) {
            return cl.getSimpleName();
        }

        return unwrap(enclosing) + "$$" + cl.getSimpleName();
    }
}
