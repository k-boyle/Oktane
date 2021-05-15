package kboyle.oktane.core.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.module.annotations.*;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.precondition.AnyPrecondition;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isStatic;
import static kboyle.oktane.core.module.factory.PreconditionFactory.NO_GROUP;

public class CommandModuleFactory<CONTEXT extends CommandContext, BASE extends ModuleBase<CONTEXT>> {
    private final BeanProvider beanProvider;
    private final Map<Class<?>, TypeParser<?>> typeParserByClass;
    private final PreconditionFactoryMap preconditionFactoryMap;

    public CommandModuleFactory(
            BeanProvider beanProvider,
            Map<Class<?>, TypeParser<?>> typeParserByClass,
            PreconditionFactoryMap preconditionFactoryMap) {
        this.beanProvider = beanProvider;
        this.typeParserByClass = typeParserByClass;
        this.preconditionFactoryMap = preconditionFactoryMap;
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

        Aliases groups = null;
        boolean singleton = false;
        boolean synchronised = false;
        var preconditionsByGroup = HashMultimap.<Object, Precondition>create();
        for (var annotation : moduleClass.getAnnotations()) {
            if (annotation instanceof Aliases aliases) {
                groups = aliases;
                for (var group : groups.value()) {
                    moduleBuilder.withGroup(group);
                }
            } else if (annotation instanceof Description description) {
                Preconditions.checkState(!Strings.isNullOrEmpty(description.value()), "A module description must be non-empty.");
                moduleBuilder.withDescription(description.value());
            } else if (annotation instanceof Name name) {
                Preconditions.checkState(!Strings.isNullOrEmpty(name.value()), "A module name must be non-empty.");
                moduleBuilder.withName(name.value());
            } else if (annotation instanceof Singleton) {
                singleton = true;
                moduleBuilder.singleton();
            } else if (annotation instanceof Synchronised) {
                synchronised = true;
                moduleBuilder.synchronised();
            } else {
                preconditionFactoryMap.handle(annotation, preconditionsByGroup::put);
            }
        }

        preconditionsByGroup.removeAll(NO_GROUP).forEach(moduleBuilder::withPrecondition);
        preconditionsByGroup.asMap().values().stream()
            .map(preconditionCollection -> new AnyPrecondition(ImmutableList.copyOf(preconditionCollection)))
            .forEach(moduleBuilder::withPrecondition);

        var moduleLock = synchronised ? new Object() : null;

        var commandFactory = new CommandFactory<>(
            preconditionFactoryMap,
            typeParserByClass,
            moduleClass,
            singleton,
            moduleLock,
            beanProvider
        );

        var methods = moduleClass.getMethods();
        for (var method : methods) {
            var command = commandFactory.createCommand(groups, method);
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
