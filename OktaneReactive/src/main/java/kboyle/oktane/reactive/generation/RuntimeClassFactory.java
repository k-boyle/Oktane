package kboyle.oktane.reactive.generation;

import com.google.common.base.Preconditions;
import kboyle.oktane.reactive.exceptions.FailedToInstantiateRuntimeModule;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.List;

public final class RuntimeClassFactory {
    private static final JavaCompiler JAVA_COMPILER = ToolProvider.getSystemJavaCompiler();
    private static final StandardJavaFileManager STANDARD_JAVA_FILE_MANAGER = JAVA_COMPILER.getStandardFileManager(null, null, null);
    private static final String CLASS_PATH;
    private static final List<String> COMPILATION_OPTIONS;

    static {
        String classPath = System.getProperty("java.class.path");

        if (classPath.endsWith(".jar")) {
            CLASS_PATH = System.getProperty("oktane.classpath");
        } else {
            CLASS_PATH = classPath;
        }

        System.out.println("cp: " + CLASS_PATH);

        COMPILATION_OPTIONS = List.of(
            "--release",
            System.getProperty("java.specification.version"),
            "-g",
            "-proc:none",
            "-classpath",
            CLASS_PATH
        );
    }

    private RuntimeClassFactory() {
    }

    public static <T> T compile(Class<T> clazz, String name, String code, Object[] ctorArgs) {
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        SourceFile sourceFile = new SourceFile(URI.create(name), code);
        Iterable<JavaFileObject> compilationUnits = List.of(sourceFile);
        FileManager fileManager = new FileManager(STANDARD_JAVA_FILE_MANAGER);
        CompilationTask compilationTask = JAVA_COMPILER.getTask(
            null,
            fileManager,
            diagnosticCollector,
            COMPILATION_OPTIONS,
            null,
            compilationUnits
        );

        Preconditions.checkState(
            compilationTask.call(),
            "Failed to generate class due to %s",
            diagnosticCollector.getDiagnostics()
        );

        byte[] byteCode = fileManager.output.outputStream.toByteArray();
        var classLoader = new ClassLoader(clazz.getClassLoader()) {
            final Class<?> compiledClazz = defineClass(null, byteCode, 0, byteCode.length);
        };

        Class<? extends T> generatedClazz = classLoader.compiledClazz.asSubclass(clazz);
        try {
            Constructor<?>[] generatedConstructors = generatedClazz.getDeclaredConstructors();
            Preconditions.checkState(
                generatedConstructors.length == 1,
                "A single public constructor must be present"
            );
            Object generatedClassInstance = generatedConstructors[0].newInstance(ctorArgs);
            return clazz.cast(generatedClassInstance);
        } catch (Exception ex) {
            throw new FailedToInstantiateRuntimeModule(ex);
        }
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
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            output = new ClassFile(URI.create(className));
            return output;
        }
    }
}
