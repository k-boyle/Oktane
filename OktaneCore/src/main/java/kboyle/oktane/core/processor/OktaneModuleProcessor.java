package kboyle.oktane.core.processor;

import com.google.auto.service.AutoService;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.exceptions.RuntimeIOException;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javax.tools.Diagnostic.Kind.ERROR;
import static kboyle.oktane.core.processor.ProcessorUtil.getBaseGeneric;
import static kboyle.oktane.core.processor.ProcessorUtil.getNestedPath;
import static kboyle.oktane.core.processor.ProcessorUtil.getPackage;

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
        if (roundEnv.processingOver() || roundEnv.errorRaised()) {
            return false;
        }

        roundEnv.getRootElements().stream()
            .mapMulti(this::flattenElement)
            .forEach(this::createCommandCallback);

        return false;
    }

    private void print(Diagnostic.Kind kind, String message, Object... args) {
        processingEnv.getMessager().printMessage(kind, String.format(message, args));
    }

    private void flattenElement(Element element, Consumer<Element> downstream) {
        if (!isPotentialModule(element)) {
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

    private void createCommandCallback(Element commandModule) {
        var contextType = getBaseGeneric(types, commandModule.asType(), moduleBaseType);
        if (contextType == null) {
            print(ERROR, "Failed to unwrap context type for %s", commandModule);
        }

        var constructor = getConstructor(commandModule);

        assert contextType != null;
        var context = contextType.toString();

        var classWriter = new CommandCallbackClassWriter(commandModule, constructor, context);
        commandModule.getEnclosedElements().stream()
            .filter(ExecutableElement.class::isInstance)
            .map(ExecutableElement.class::cast)
            .filter(element -> element.getModifiers().contains(Modifier.PUBLIC))
            .map(this::getMethodData)
            .filter(MethodData::isValid)
            .forEach(method -> createClass(commandModule, classWriter, method));
    }

    private void createClass(Element commandModule, CommandCallbackClassWriter commandCallbackClassWriter, MethodData data) {
        var method = data.method();
        var classPackage = getPackage(commandModule);
        var callbackClassname = getGeneratedClassName(commandModule, method);

        try {
            var javaFileObject = filer.createSourceFile(callbackClassname);

            try (var writer = new PrintWriter(javaFileObject.openWriter())) {
                commandCallbackClassWriter.write(writer, callbackClassname, classPackage, data);
            }
        } catch (IOException ex) {
            throw new RuntimeIOException(ex);
        }
    }

    private String getGeneratedClassName(Element commandModule, ExecutableElement method) {
        var nestedPath = getNestedPath(commandModule, "$$");

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

    private ExecutableElement getConstructor(Element element) {
        return element.getEnclosedElements().stream()
            .filter(enclosed -> enclosed.getKind() == ElementKind.CONSTRUCTOR)
            .map(ExecutableElement.class::cast)
            .findFirst()
            .orElseThrow();
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
