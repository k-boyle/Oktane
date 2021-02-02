package kb.octane.core.module;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import kb.octane.core.BeanProvider;
import kb.octane.core.CommandContext;
import kb.octane.core.exceptions.FailedToInstantiateRuntimeModule;
import kb.octane.core.results.command.CommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class CommandCallbackFactory {
    private static final String OBSOLETE = "";

    private static final String IMPORT_TEMPLATE = "import %s.%s;\n";
    private static final String CLASS_NAME_TEMPLATE = "%s%s%d";
    private static final String BEANS_CAST_TEMPLATE = "(%s)beans[%d]";
    private static final String PARAMETER_CAST_TEMPLATE = "(%s)parameters[%d]";
    private static final String FIELD = "private final %s %s;";
    private static final String ASSIGNMENT = "this.%1$s = %1$s;";
    private static final String CTOR_PARAM = "%s %s";
    private static final String MODULE_INLINE_INIT = "%1$s module = new %1$s(%2$s);";

    private static final String CLASS_TEMPLATE = """
        package %1$s;
        
        import %12$s.%11$s;
        import %13$s.%3$s;
        import %14$s.%4$s;
        import %15$s.%5$s;
        import %16$s.%6$s;
        import %19$s.%18$s;
        
        %17$s
        
        public final class %2$s implements %11$s {
            %20$s
            
            public %2$s(%22$s) {
                %23$s
            }
        
            @Override
            public %21$s%3$s<%4$s> execute(%5$s context, Object[] beans, Object[] parameters) {
                %8$s
                %7$s
                module.setContext((%18$s) context);
                try {
                    return module.%9$s(%10$s);
                } catch (Exception ex) {
                    return %3$s.error(ex);
                }
                %24$s
            }
        }        
        """;

    private static final String CLASSPATH = System.getProperty("java.class.path");
    private static final String OBJECT = "Object";
    private static final String LOCK = "lock";
    private static final String MODULE = "module";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JavaCompiler javaCompiler;
    private final StandardJavaFileManager standardJavaFileManager;

    public CommandCallbackFactory() {
        this.javaCompiler = ToolProvider.getSystemJavaCompiler();
        this.standardJavaFileManager = javaCompiler.getStandardFileManager(null, null, null);
    }

    // todo clean this up, not really important rn but it's big spaghet
    @SuppressWarnings("rawtypes")
    public <T extends CommandContext> CommandCallback createCommandCallback(
            Class<T> concreteCommandContextClazz,
            Class<? extends CommandModuleBase<T>> moduleClazz,
            boolean singleton,
            Object moduleLock,
            boolean commandSynchronised,
            Method method,
            BeanProvider beanProvider) {
        logger.trace("Creating command callback from {}", method.getName());

        Constructor<?>[] constructors = moduleClazz.getConstructors();
        Preconditions.checkState(constructors.length == 1, "There must be only 1 public constructor");

        StringBuilder additionalImports = new StringBuilder();

        Constructor<?> constructor = constructors[0];

        Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
        for (Class<?> constructorParameterType : constructorParameterTypes) {
            if (constructorParameterType.isPrimitive()) {
                continue;
            }
            additionalImports.append(String.format(IMPORT_TEMPLATE, constructorParameterType.getPackageName(), constructorParameterType.getSimpleName()));
        }

        String moduleSimpleName = moduleClazz.getSimpleName();
        String modulePackage = moduleClazz.getPackageName();

        String generatedClassName = String.format(
            CLASS_NAME_TEMPLATE,
            moduleSimpleName,
            method.getName(),
            System.nanoTime()
        );

        CommandModuleBase<T> module = getModule(singleton, moduleClazz, beanProvider);

        StringJoiner fields = new StringJoiner("\n    ");
        StringJoiner ctorArgs = new StringJoiner(", ");
        StringJoiner ctorAssignment = new StringJoiner("\n        ");

        List<Object> ctorParams = new ArrayList<>();

        if (module != null) {
            fields.add(String.format(FIELD, moduleClazz.getSimpleName(), MODULE));
            ctorArgs.add(String.format(CTOR_PARAM, moduleClazz.getSimpleName(), MODULE));
            ctorAssignment.add(String.format(ASSIGNMENT, MODULE));
            ctorParams.add(module);
        }

        if (moduleLock != null) {
            fields.add(String.format(FIELD, OBJECT, LOCK));
            ctorArgs.add(String.format(CTOR_PARAM, OBJECT, LOCK));
            ctorAssignment.add(String.format(ASSIGNMENT, LOCK));
            ctorParams.add(moduleLock);
        }

        String moduleInlineInit = module != null
            ? OBSOLETE
            : String.format(MODULE_INLINE_INIT, moduleSimpleName, deconstructBeans(constructorParameterTypes));

        Class<?>[] methodParameterTypes = method.getParameterTypes();
        for (Class<?> methodParameterType : methodParameterTypes) {
            if (methodParameterType.isPrimitive()) {
                continue;
            }
            additionalImports.append(String.format(IMPORT_TEMPLATE, methodParameterType.getPackageName(), methodParameterType.getSimpleName()));
        }

        String parametersDestructed = Streams.zip(
            Arrays.stream(methodParameterTypes),
            IntStream.range(0, methodParameterTypes.length).boxed(),
            (parameter, index) -> String.format(PARAMETER_CAST_TEMPLATE, parameter.getSimpleName(), index)
        )
            .collect(Collectors.joining(", "));

        String package0 = CommandCallbackFactory.class.getPackageName();

        Class<CommandCallback> commandCallbackClazz = CommandCallback.class;
        String commandCallbackSimpleName = commandCallbackClazz.getSimpleName();
        String commandCallbackPackage = commandCallbackClazz.getPackageName();

        Class<Mono> monoClazz = Mono.class;
        String monoSimpleName = monoClazz.getSimpleName();
        String monoPackage = monoClazz.getPackageName();

        Class<CommandResult> commandResultClazz = CommandResult.class;
        String commandResultSimpleName = commandResultClazz.getSimpleName();
        String commandResultPackage = commandResultClazz.getPackageName();

        Class<CommandContext> commandContextClazz = CommandContext.class;
        String commandContextSimpleName = commandContextClazz.getSimpleName();
        String commandContextPackage = commandContextClazz.getPackageName();


        String concreteCommandContextSimpleName = concreteCommandContextClazz.getSimpleName();
        String concreteCommandContextPackage = concreteCommandContextClazz.getPackageName();

        String methodName = method.getName();

        String code = String.format(
            CLASS_TEMPLATE,
            package0,
            generatedClassName,
            monoSimpleName,
            commandResultSimpleName,
            commandContextSimpleName,
            moduleSimpleName,
            moduleInlineInit,
            moduleLock != null ? "synchronized(lock) {" : OBSOLETE,
            methodName,
            parametersDestructed,
            commandCallbackSimpleName,
            commandCallbackPackage,
            monoPackage,
            commandResultPackage,
            commandContextPackage,
            modulePackage,
            additionalImports.toString(),
            concreteCommandContextSimpleName,
            concreteCommandContextPackage,
            fields.toString(),
            commandSynchronised ? "synchronized " : OBSOLETE,
            ctorArgs.toString(),
            ctorAssignment.toString(),
            moduleLock != null ? "}" : OBSOLETE
        );

        logger.trace("Generated code\n{}", code);

        // based on https://github.com/medallia/javaone2016/blob/master/src/main/java/com/medallia/codegen/JavaCodeGenerator.java
        List<JavaFileObject> compilationUnits = Collections.singletonList(new SourceFile(URI.create(generatedClassName), code));
        FileManager fileManager = new FileManager(standardJavaFileManager);
        StringWriter output = new StringWriter();
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        List<String> options = List.of(
            "--release",
            System.getProperty("java.specification.version"),
            "--enable-preview",
            "-g",
            "-proc:none",
            "-classpath",
            CLASSPATH
        );
        CompilationTask compilationTask = javaCompiler.getTask(
            output,
            fileManager,
            diagnosticCollector,
            options,
            null,
            compilationUnits
        );

        logger.trace("Compiling class {}", generatedClassName);

        boolean success = compilationTask.call();
        Preconditions.checkState(
            success,
            "Failed to generate command class due to %s",
            diagnosticCollector.getDiagnostics()
        );
        byte[] byteCode = fileManager.output.outputStream.toByteArray();
        var classLoader = new ClassLoader(moduleClazz.getClassLoader()) {
            Class<?> clazz = defineClass(null, byteCode, 0, byteCode.length);
        };

        logger.trace("Creating generated class {}", generatedClassName);
        Class<? extends CommandCallback> generatedClass = classLoader.clazz.asSubclass(CommandCallback.class);
        try {
            Constructor<?> callbackConstructor = generatedClass.getDeclaredConstructors()[0];
            return (CommandCallback) (!ctorParams.isEmpty() ? callbackConstructor.newInstance(ctorParams.toArray()) : callbackConstructor.newInstance());
        } catch (Exception ex) {
            throw new FailedToInstantiateRuntimeModule(ex);
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

    private String deconstructBeans(Class<?>[] constructorParameterTypes) {
        return Streams.zip(
            Arrays.stream(constructorParameterTypes),
            IntStream.range(0, constructorParameterTypes.length).boxed(),
            (parameter, index) -> String.format(BEANS_CAST_TEMPLATE, parameter.getSimpleName(), index)
        )
            .collect(Collectors.joining(", "));
    }

    private static class SourceFile extends SimpleJavaFileObject {
        private final String contents;

        private SourceFile(URI uri, String contents) {
            super(uri, Kind.SOURCE);
            this.contents = contents;
        }

        @Override
        public String getName() { return uri.getRawSchemeSpecificPart(); }

        @Override
        public boolean isNameCompatible(String simpleName, Kind kind) { return true; }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return contents;
        }
    }

    private static class ClassFile extends SimpleJavaFileObject {
        private ByteArrayOutputStream outputStream;

        private ClassFile(URI uri) {
            super(uri, Kind.CLASS);
        }

        @Override
        public String getName() {
            return uri.getRawSchemeSpecificPart();
        }

        @Override
        public OutputStream openOutputStream() {
            return outputStream = new ByteArrayOutputStream();
        }
    }

    private static class FileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private ClassFile output;

        private FileManager(StandardJavaFileManager target) {
            super(target);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) {
            output = new ClassFile(URI.create(className));
            return output;
        }
    }
}
