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
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedAnnotationTypes("kboyle.oktane.core.module.annotations.Aliases")
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
        var elements = processingEnv.getElementUtils();

        var moduleBase = elements.getTypeElement(ModuleBase.class.getName());
        var context = elements.getTypeElement(CommandContext.class.getName());
        var baseGeneric = types.getWildcardType(context.asType(), null);
        this.moduleBaseType = types.getDeclaredType(moduleBase, baseGeneric);

        var commandResult = elements.getTypeElement(CommandResult.class.getName()).asType();
        var mono = elements.getTypeElement(Mono.class.getName());
        this.monoCommandReturnType = types.getDeclaredType(mono, commandResult);

        this.commandReturnType = commandResult;

        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        print(NOTE, "");

        var allSuccess = roundEnv.getRootElements().stream()
            .mapMulti(this::flattenElement)
            .allMatch(this::createCommandCallback);

        if (!allSuccess) {
            return false;
        }

        print(NOTE, "");
        return true;
    }

    private void print(Diagnostic.Kind kind, String message, Object... args) {
        processingEnv.getMessager().printMessage(kind, String.format(message, args));
    }

    private void flattenElement(Element element, Consumer<Element> downstream) {
        if (!isPotentialModule(element)) {
            print(NOTE, "%s is not a potential module, skipping", element);
            return;
        }

        downstream.accept(element);
        for (var enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement instanceof TypeElement) {
                flattenElement(enclosedElement, downstream);
            }
        }
    }

    private boolean isPotentialModule(Element element) {
        return types.isSubtype(element.asType(), moduleBaseType)
            && !element.getModifiers().contains(Modifier.ABSTRACT)
            && element.getModifiers().contains(Modifier.PUBLIC);
    }

    private boolean createCommandCallback(Element commandModule) {
        print(NOTE, "Processing annotated class %s", commandModule);

        var contextType = getContextType(commandModule);
        if (contextType == null) {
            print(ERROR, "Failed to unwrap context type for %s", commandModule);
            return false;
        }

        var constructor = getConstructor(commandModule);

        var context = contextType.toString();

        var classWriter = new ClassWriter(commandModule, constructor, context);
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
        var method = data.method();
        var classPackage = getPackage(commandModule);
        var callbackClassname = getGeneratedClassName(commandModule, method);

        try {
            var javaFileObject = filer.createSourceFile(callbackClassname);

            try (var writer = new PrintWriter(javaFileObject.openWriter())) {
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
        var nestedPath = getNestedPath(commandModule);

        var parameterNameString = method.getParameters().stream()
            .map(variableElement -> {
                var typeMirror = variableElement.asType();
                var typeStr = typeMirror.toString();
                return typeStr.replace(".", "0")
                    .replace("<", "$$")
                    .replace(">", "$$");
            })
            .collect(Collectors.joining("_"));

        return String.join("$", nestedPath, method.getSimpleName(), parameterNameString);
    }

    private String getPackage(Element element) {
        var enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement packageElement) {
            return packageElement.toString();
        }

        return getPackage(enclosingElement);
    }

    private String getNestedPath(Element commandModule) {
        var enclosingElement = commandModule.getEnclosingElement();
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
        var returnType = element.getReturnType();
        var monoReturn = types.isSameType(returnType, monoCommandReturnType);

        return new MethodData(
            element,
            monoReturn || types.isSameType(returnType, commandReturnType),
            monoReturn
        );
    }
}
