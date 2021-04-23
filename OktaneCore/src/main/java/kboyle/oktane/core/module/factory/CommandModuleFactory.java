package kboyle.oktane.core.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.CommandUtils;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.annotations.*;
import kboyle.oktane.core.parsers.TypeParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isStatic;

public class CommandModuleFactory<CONTEXT extends CommandContext, BASE extends ModuleBase<CONTEXT>> {
    private final BeanProvider beanProvider;
    private final Map<Class<?>, TypeParser<?>> typeParserByClass;

    public CommandModuleFactory(BeanProvider beanProvider, Map<Class<?>, TypeParser<?>> typeParserByClass) {
        this.beanProvider = beanProvider;
        this.typeParserByClass = typeParserByClass;
    }

    public <MODULE extends BASE> CommandModule create(Class<MODULE> moduleClass) {
        CommandModule.Builder moduleBuilder = createBuilder(moduleClass);
        return moduleBuilder.build();
    }

    private <MODULE extends BASE> CommandModule.Builder createBuilder(Class<MODULE> moduleClass) {
        Preconditions.checkState(!Modifier.isAbstract(moduleClass.getModifiers()), "A module cannot be abstract");

        CommandModule.Builder moduleBuilder = CommandModule.builder()
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

        CommandUtils.createPreconditions(moduleClass).forEach(moduleBuilder::withPrecondition);

        Name moduleName = moduleClass.getAnnotation(Name.class);
        if (moduleName != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(moduleName.value()), "A module name must be non-empty.");
            moduleBuilder.withName(moduleName.value());
        }

        CommandFactory<CONTEXT, MODULE> commandFactory = new CommandFactory<>(
            typeParserByClass,
            moduleClass,
            singleton,
            moduleLock,
            beanProvider
        );

        Method[] methods = moduleClass.getMethods();
        for (Method method : methods) {
            Command.Builder command = commandFactory.createCommand(moduleGroups, method);
            if (command != null) {
                moduleBuilder.withCommand(command);
            }
        }

        createChildren(moduleClass).forEach(moduleBuilder::withChild);
        return moduleBuilder;
    }

    @SuppressWarnings("unchecked")
    private <MODULE extends BASE> Stream<CommandModule.Builder> createChildren(Class<MODULE> moduleClass) {
        Class<? extends BASE> moduleBaseClass = (Class<? extends BASE>) moduleClass.getSuperclass();
        return Arrays.stream(moduleClass.getDeclaredClasses())
            .filter(cl -> isStatic(cl.getModifiers()))
            .filter(cl -> cl.getSuperclass() != moduleClass)
            .filter(moduleBaseClass::isAssignableFrom)
            .map(cl -> createBuilder((Class<? extends BASE>) cl));
    }
}
