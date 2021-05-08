package kboyle.oktane.core.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.CommandUtils;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.annotations.*;
import kboyle.oktane.core.parsers.TypeParser;

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
        var moduleBuilder = createBuilder(moduleClass);
        return moduleBuilder.build();
    }

    private <MODULE extends BASE> CommandModule.Builder createBuilder(Class<MODULE> moduleClass) {
        Preconditions.checkState(!Modifier.isAbstract(moduleClass.getModifiers()), "A module cannot be abstract");

        var moduleBuilder = CommandModule.builder()
            .withName(moduleClass.getSimpleName());

        var constructors = moduleClass.getConstructors();
        Preconditions.checkState(constructors.length == 1, "There must be only 1 public constructor");

        var constructor = constructors[0];

        var constructorParameterTypes = constructor.getParameterTypes();
        for (var parameterType : constructorParameterTypes) {
            moduleBuilder.withBean(parameterType);
        }

        var moduleGroups = moduleClass.getAnnotation(Aliases.class);
        if (moduleGroups != null) {
            for (var group : moduleGroups.value()) {
                moduleBuilder.withGroup(group);
            }
        }

        var singleton = moduleClass.getAnnotation(Singleton.class) != null;
        var moduleSynchronised = moduleClass.getAnnotation(Synchronised.class) != null;
        moduleBuilder.withSingleton(singleton);
        moduleBuilder.withSynchronised(moduleSynchronised);
        var moduleLock = moduleSynchronised ? new Object() : null;

        var moduleDescription = moduleClass.getAnnotation(Description.class);
        if (moduleDescription != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(moduleDescription.value()), "A module description must be non-empty.");
            moduleBuilder.withDescription(moduleDescription.value());
        }

        CommandUtils.createPreconditions(moduleClass).forEach(moduleBuilder::withPrecondition);

        var moduleName = moduleClass.getAnnotation(Name.class);
        if (moduleName != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(moduleName.value()), "A module name must be non-empty.");
            moduleBuilder.withName(moduleName.value());
        }

        var commandFactory = new CommandFactory<>(
            typeParserByClass,
            moduleClass,
            singleton,
            moduleLock,
            beanProvider
        );

        var methods = moduleClass.getMethods();
        for (var method : methods) {
            var command = commandFactory.createCommand(moduleGroups, method);
            if (command != null) {
                moduleBuilder.withCommand(command);
            }
        }

        createChildren(moduleClass).forEach(moduleBuilder::withChild);
        return moduleBuilder;
    }

    @SuppressWarnings("unchecked")
    private <MODULE extends BASE> Stream<CommandModule.Builder> createChildren(Class<MODULE> moduleClass) {
        var moduleBaseClass = (Class<? extends BASE>) moduleClass.getSuperclass();
        return Arrays.stream(moduleClass.getDeclaredClasses())
            .filter(cl -> isStatic(cl.getModifiers()))
            .filter(cl -> cl.getSuperclass() != moduleClass)
            .filter(moduleBaseClass::isAssignableFrom)
            .map(cl -> createBuilder((Class<? extends BASE>) cl));
    }
}
