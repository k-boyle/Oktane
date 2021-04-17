package kboyle.oktane.reactive.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import kboyle.oktane.reactive.BeanProvider;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.CommandUtil;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.ReactiveCommandParameter;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.module.annotations.*;
import kboyle.oktane.reactive.parsers.ReactiveTypeParser;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

public class CommandFactory<T extends CommandContext, S extends ReactiveModuleBase<T>> {
    private final Map<Class<?>, ReactiveTypeParser<?>> typeParserByClass;
    private final Class<S> moduleClass;
    private final CommandCallbackFactory callbackFactory;
    private final boolean singleton;
    private final Object moduleLock;
    private final BeanProvider beanProvider;

    public CommandFactory(
            Map<Class<?>, ReactiveTypeParser<?>> typeParserByClass,
            Class<S> moduleClass,
            CommandCallbackFactory callbackFactory,
            boolean singleton,
            Object moduleLock,
            BeanProvider beanProvider) {
        this.typeParserByClass = typeParserByClass;
        this.moduleClass = moduleClass;
        this.callbackFactory = callbackFactory;
        this.singleton = singleton;
        this.moduleLock = moduleLock;
        this.beanProvider = beanProvider;
    }

    public ReactiveCommand.Builder createCommand(Aliases moduleGroups, Method method) {
        if (!isValidCommandSignature(method)) {
            return null;
        }

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
                moduleClass,
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

        CommandUtil.createPreconditions(method).forEach(commandBuilder::withPrecondition);

        CommandParameterFactory parameterFactory = new CommandParameterFactory(typeParserByClass, method);
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            ReactiveCommandParameter.Builder commandParameter = parameterFactory.createParameter(parameter);
            commandBuilder.withParameter(commandParameter);
        }

        return commandBuilder;
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
}
