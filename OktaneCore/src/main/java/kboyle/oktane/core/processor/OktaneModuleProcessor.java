package kboyle.oktane.core.processor;

import com.google.auto.service.AutoService;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedAnnotationTypes("kboyle.oktane.core.processor.OktaneModule")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
@AutoService(Processor.class)
public class OktaneModuleProcessor extends AbstractProcessor {
    private Types types;

    private TypeMirror moduleBaseType;
    private TypeMirror monoCommandReturnType;
    private TypeMirror commandReturnType;

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.types = processingEnv.getTypeUtils();
        Elements elements = processingEnv.getElementUtils();

        TypeElement moduleBase = elements.getTypeElement(ModuleBase.class.getName());
        TypeElement context = elements.getTypeElement(CommandContext.class.getName());
        WildcardType baseGeneric = types.getWildcardType(context.asType(), null);
        this.moduleBaseType = types.getDeclaredType(moduleBase, baseGeneric);

        TypeMirror commandResult = elements.getTypeElement(CommandResult.class.getName()).asType();
        TypeElement mono = elements.getTypeElement(Mono.class.getName());
        this.monoCommandReturnType = types.getDeclaredType(mono, commandResult);

        this.commandReturnType = commandResult;

        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        print(NOTE, "");

        for (TypeElement annotation : annotations) {
            print(NOTE, "Generating modules for annotation %s", annotation);

            for (Element commandModule : roundEnv.getElementsAnnotatedWith(annotation)) {
                print(NOTE, "Processing annotated class %s", commandModule);

                TypeMirror moduleType = commandModule.asType();
                if (!types.isSubtype(moduleType, moduleBaseType)) {
                    print(
                        ERROR,
                        "Module \"%s\" is annotated but doesn't extend %s",
                        commandModule.getSimpleName(),
                        moduleBaseType
                    );

                    return false;
                }

                if (commandModule.getModifiers().contains(Modifier.ABSTRACT)) {
                    print(
                        NOTE,
                        "Module \"%s\" is abstract, skipping",
                        moduleType
                    );
                    continue;
                }

                if (!createCommandCallback(commandModule)) {
                    return false;
                }
            }
        }

        print(NOTE, "");
        return true;
    }

    private void print(Diagnostic.Kind kind, String message, Object... args) {
        processingEnv.getMessager().printMessage(kind, String.format(message, args));
    }

    private boolean createCommandCallback(Element commandModule) {
        print(NOTE, "Processing annotated class %s", commandModule);

        TypeMirror contextType = getContextType(commandModule);
        if (contextType == null) {
            print(ERROR, "Failed to unwrap context type for %s", commandModule);
            return false;
        }

        ExecutableElement constructor = getConstructor(commandModule);

        String context = contextType.toString();

        ClassWriter classWriter = new ClassWriter(commandModule, constructor, context);
        commandModule.getEnclosedElements().stream()
            .filter(ExecutableElement.class::isInstance)
            .map(ExecutableElement.class::cast)
            .filter(element -> element.getModifiers().contains(Modifier.PUBLIC))
            .map(this::getMethodData)
            .filter(MethodData::isValid)
            .forEach(method -> createClass(commandModule, classWriter, method));

        return true;
    }

    private void createClass(Element commandModule, ClassWriter classWriter, MethodData data) {
        ExecutableElement method = data.method();
        String classPackage = getPackage(commandModule);
        String callbackClassname = getGeneratedClassName(commandModule, method);

        try {
            JavaFileObject javaFileObject = filer.createSourceFile(callbackClassname);

            try (PrintWriter writer = new PrintWriter(javaFileObject.openWriter())) {
                classWriter.write(writer, callbackClassname, classPackage, data);
            }
        } catch (IOException ex) {
            print(ERROR,
                "An exception was thrown whilst try to create callback for %s\n%s",
                method,
                Arrays.stream(ex.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"))
            );
        }
    }

    private String getGeneratedClassName(Element commandModule, ExecutableElement method) {
        String nestedPath = getNestedPath(commandModule);

        String parameterNameString = method.getParameters().stream()
            .map(variableElement -> {
                TypeMirror typeMirror = variableElement.asType();
                String typeStr = typeMirror.toString();
                return typeStr.replace(".", "0")
                    .replace("<", "$$")
                    .replace(">", "$$");
            })
            .collect(Collectors.joining("_"));

        return String.join("$", nestedPath, method.getSimpleName(), parameterNameString);
    }

    private String getPackage(Element element) {
        Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement packageElement) {
            return packageElement.toString();
        }

        return getPackage(enclosingElement);
    }

    private String getNestedPath(Element commandModule) {
        Element enclosingElement = commandModule.getEnclosingElement();
        if (enclosingElement.getKind() == ElementKind.PACKAGE) {
            return commandModule.getSimpleName().toString();
        }

        return getNestedPath(enclosingElement) + "$$" + commandModule.getSimpleName();
    }

    private ExecutableElement getConstructor(Element element) {
        return element.getEnclosedElements().stream()
            .filter(enclosed -> enclosed.getKind() == ElementKind.CONSTRUCTOR)
            .map(ExecutableElement.class::cast)
            .findFirst()
            .orElseThrow();
    }

    private TypeMirror getContextType(Element element) {
        if (element instanceof TypeElement typeElement) {
            if (typeElement.getSuperclass() instanceof DeclaredType declaredType) {
                 if (types.isSameType(types.erasure(moduleBaseType), types.erasure(declaredType))) {
                     return declaredType.getTypeArguments().get(0);
                 }

                 return getContextType(declaredType.asElement());
            }

            print(ERROR, "Unknown Element found %s", typeElement);
        }

        print(ERROR, "Unknown Element found %s", element);
        return null;
    }

    private MethodData getMethodData(ExecutableElement element) {
        TypeMirror returnType = element.getReturnType();
        boolean monoReturn = types.isSameType(returnType, monoCommandReturnType);

        return new MethodData(
            element,
            monoReturn || types.isSameType(returnType, commandReturnType),
            monoReturn
        );
    }
}
