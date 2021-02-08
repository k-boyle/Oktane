package kboyle.octane.core.module;

import com.google.common.base.Preconditions;
import kboyle.octane.core.BeanProvider;
import kboyle.octane.core.CommandContext;
import kboyle.octane.core.generation.AccessModifier;
import kboyle.octane.core.generation.ClassGenerator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CommandCallbackFactory0 {
    private static final String COMMAND_METHOD = """
        @Override
        public CommandResult %1$s execute(CommandContext context, Object[] beans, Object[] services) {
            try {
            
            } catch (Exception ex) {
                return module.error(ex);
            }
        }
        """;

    public <T extends CommandContext> void createCommandCallback(
            Class<T> concreteCommandContextClazz,
            Class<? extends CommandModuleBase<T>> moduleClazz,
            boolean singleton,
            Object moduleLock,
            boolean commandSynchronised,
            Method method,
            BeanProvider beanProvider) {
        Constructor<?>[] constructors = moduleClazz.getConstructors();
        Preconditions.checkState(constructors.length == 1, "There must be only 1 public constructor");

        String generatedName = moduleClazz.getSimpleName() + method.getName() + System.nanoTime();
        ClassGenerator classGenerator = new ClassGenerator()
            .withPackage(getClass().getPackageName())
            .withAccess(AccessModifier.PUBLIC)
            .withName(generatedName)
            .withImplementation(CommandCallback.class);

        Constructor<?> constructor = constructors[0];

        Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
        for (Class<?> constructorParameterType : constructorParameterTypes) {
            String name = constructorParameterType.getSimpleName();
            classGenerator.withField(constructorParameterType, name, AccessModifier.PRIVATE, true);

        }

        CommandModuleBase<T> module = getModule(singleton, moduleClazz, beanProvider);
        List<Object> constructorParameters = new ArrayList<>();

        if (module != null) {
            classGenerator.withField(moduleClazz, "module", AccessModifier.PRIVATE, true);
            constructorParameters.add(module);
        }

        if (moduleLock != null) {
            classGenerator.withField(Object.class, "lock", AccessModifier.PRIVATE, true);
            constructorParameters.add(moduleLock);
        }


    }

    private static <T extends CommandContext> CommandModuleBase<T> getModule(
            boolean singleton,
            Class<? extends CommandModuleBase<T>> moduleClazz,
            BeanProvider beanProvider) {
        if (!singleton) {
            return null;
        }

        return Preconditions.checkNotNull(
            beanProvider.getBean(moduleClazz),
            "Singleton module must be supplied by the beanProvider"
        );
    }
}
