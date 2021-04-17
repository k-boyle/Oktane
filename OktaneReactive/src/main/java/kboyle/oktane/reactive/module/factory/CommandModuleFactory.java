package kboyle.oktane.reactive.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import kboyle.oktane.reactive.BeanProvider;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.CommandUtil;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.ReactiveModule;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.module.annotations.*;
import kboyle.oktane.reactive.parsers.ReactiveTypeParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class CommandModuleFactory<CONTEXT extends CommandContext, BASE extends ReactiveModuleBase<CONTEXT>> {
    private final BeanProvider beanProvider;
    private final Map<Class<?>, ReactiveTypeParser<?>> typeParserByClass;
    private final CommandCallbackFactory callbackFactory;

    public CommandModuleFactory(
            BeanProvider beanProvider,
            Map<Class<?>, ReactiveTypeParser<?>> typeParserByClass) {
        this.beanProvider = beanProvider;
        this.typeParserByClass = typeParserByClass;
        this.callbackFactory = new CommandCallbackFactory();
    }

    public <MODULE extends BASE> ReactiveModule create(Class<MODULE> moduleClass) {
        ReactiveModule.Builder moduleBuilder = createBuilder(moduleClass);
        return moduleBuilder.build();
    }

    private <MODULE extends BASE> ReactiveModule.Builder createBuilder(Class<MODULE> moduleClass) {
        Preconditions.checkState(!Modifier.isAbstract(moduleClass.getModifiers()), "A module cannot be abstract");

        ReactiveModule.Builder moduleBuilder = ReactiveModule.builder()
            .withName(moduleClass.getSimpleName());

        Constructor<?>[] constructors = moduleClass.getConstructors();
        Preconditions.checkState(constructors.length == 1, "There must be only 1 public constructor");

        Constructor<?> constructor = constructors[0];

        Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
        for (Class<?> parameterType : constructorParameterTypes) {
            moduleBuilder.withBean(parameterType);
        }

        Aliases moduleGroups = moduleClass.getAnnotation(Aliases.class);
        if (moduleGroups != null) {
            for (String group : moduleGroups.value()) {
                moduleBuilder.withGroup(group);
            }
        }

        boolean singleton = moduleClass.getAnnotation(Singleton.class) != null;
        boolean moduleSynchronised = moduleClass.getAnnotation(Synchronised.class) != null;
        moduleBuilder.withSingleton(singleton);
        moduleBuilder.withSynchronised(moduleSynchronised);
        Object moduleLock = moduleSynchronised ? new Object() : null;

        Description moduleDescription = moduleClass.getAnnotation(Description.class);
        if (moduleDescription != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(moduleDescription.value()), "A module description must be non-empty.");
            moduleBuilder.withDescription(moduleDescription.value());
        }

        CommandUtil.createPreconditions(moduleClass).forEach(moduleBuilder::withPrecondition);

        Name moduleName = moduleClass.getAnnotation(Name.class);
        if (moduleName != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(moduleName.value()), "A module name must be non-empty.");
            moduleBuilder.withName(moduleName.value());
        }

        CommandFactory<CONTEXT, MODULE> commandFactory = new CommandFactory<>(
            typeParserByClass,
            moduleClass,
            callbackFactory,
            singleton,
            moduleLock,
            beanProvider
        );

        Method[] methods = moduleClass.getMethods();
        for (Method method : methods) {
            ReactiveCommand.Builder command = commandFactory.createCommand(moduleGroups, method);
            if (command != null) {
                moduleBuilder.withCommand(command);
            }
        }

        createChildren(moduleClass).forEach(moduleBuilder::withChild);
        return moduleBuilder;
    }

    @SuppressWarnings("unchecked")
    private <MODULE extends BASE> Stream<ReactiveModule.Builder> createChildren(Class<MODULE> moduleClass) {
        Class<? extends BASE> moduleBaseClass = (Class<? extends BASE>) moduleClass.getSuperclass();
        return Arrays.stream(moduleClass.getDeclaredClasses())
            .filter(moduleBaseClass::isAssignableFrom)
            .map(cl -> createBuilder((Class<? extends BASE>) cl));
    }
}
