package kboyle.oktane.core.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.CommandUtils;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.annotations.*;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.precondition.PreconditionFactory;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isStatic;

public class CommandModuleFactory<CONTEXT extends CommandContext, BASE extends ModuleBase<CONTEXT>> {
    private final BeanProvider beanProvider;
    private final Map<Class<?>, TypeParser<?>> typeParserByClass;
    private final ImmutableMap<Class<?>, PreconditionFactory<?>> preconditionFactoryByClass;

    public CommandModuleFactory(
            BeanProvider beanProvider,
            Map<Class<?>, TypeParser<?>> typeParserByClass,
            ImmutableMap<Class<?>, PreconditionFactory<?>> preconditionFactoryByClass) {
        this.beanProvider = beanProvider;
        this.typeParserByClass = typeParserByClass;
        this.preconditionFactoryByClass = preconditionFactoryByClass;
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
                var preconditionFactory = preconditionFactoryByClass.get(annotation.annotationType());
                if (preconditionFactory != null) {
                    var precondition = Preconditions.checkNotNull(
                        preconditionFactory.createPrecondition0(annotation),
                        "A PreconditionFactory cannot return null"
                    );
                    moduleBuilder.withPrecondition(precondition);
                }
            }
        }

        var moduleLock = synchronised ? new Object() : null;

        CommandUtils.createPreconditions(moduleClass).forEach(moduleBuilder::withPrecondition);

        var commandFactory = new CommandFactory<>(
            preconditionFactoryByClass,
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
