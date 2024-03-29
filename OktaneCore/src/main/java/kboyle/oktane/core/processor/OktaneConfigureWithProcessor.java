package kboyle.oktane.core.processor;

import com.google.auto.service.AutoService;
import kboyle.oktane.core.exceptions.RuntimeIOException;
import kboyle.oktane.core.module.factory.PreconditionFactory;
import kboyle.oktane.core.parsers.TypeParser;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;
import static kboyle.oktane.core.processor.ProcessorUtil.getBaseGeneric;
import static kboyle.oktane.core.processor.ProcessorUtil.getNestedPath;
import static kboyle.oktane.core.processor.ProcessorUtil.getPackage;

@SupportedAnnotationTypes("kboyle.oktane.core.processor.ConfigureWith")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
@AutoService(Processor.class)
public class OktaneConfigureWithProcessor extends AbstractProcessor {
    private Types types;

    private TypeMirror typeParserType;
    private TypeMirror preconditionFactoryType;

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        var elements = processingEnv.getElementUtils();

        types = processingEnv.getTypeUtils();

        var wildcard = types.getWildcardType(null, null);
        var parserBase = elements.getTypeElement(TypeParser.class.getName());
        typeParserType = types.getDeclaredType(parserBase, wildcard);
        preconditionFactoryType = types.getDeclaredType(elements.getTypeElement(PreconditionFactory.class.getName()), wildcard);

        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver() || roundEnv.errorRaised()) {
            return false;
        }

        for (var annotation : annotations) {
            for (var element : roundEnv.getElementsAnnotatedWith(annotation)) {
                var className = "CommandHandlerWith" + getNestedPath(element, "");

                var elementType = element.asType();
                validateNoGenerics(element, elementType);
                validateDefaultConstructor(element);

                var packageName = getPackage(element);
                var configureWith = element.getAnnotation(ConfigureWith.class);

                var builder = getClassBuilder(element, className, elementType, packageName, configureWith);
                if (builder == null) {
                    continue;
                }

                try {
                    var javaFileObject = filer.createSourceFile(className);
                    try (var writer = new PrintWriter(javaFileObject.openWriter())) {
                        writer.print(builder);
                    }
                } catch (IOException ex) {
                    throw new RuntimeIOException(ex);
                }
            }
        }

        return false;
    }

    private CommandHandlerWithClassBuilder getClassBuilder(
            Element element,
            String className,
            TypeMirror elementType,
            String packageName,
            ConfigureWith configureWith) {
        var builder = new CommandHandlerWithClassBuilder(element, packageName, className, configureWith.priority());
        if (types.isSubtype(elementType, typeParserType)) {
            var targetType = getBaseGeneric(types, elementType, typeParserType);
            if (targetType == null) {
                print(ERROR, "Failed to unwrap target type for %s", element);
            }

            builder.appendTypeParser(targetType);
        } else if (types.isSubtype(elementType, preconditionFactoryType)) {
            builder.appendPreconditionFactory();
        } else {
            print(WARNING, "%s is not a supported @ConfigureWith, skipping", element);
            return null;
        }

        return builder;
    }

    private void validateDefaultConstructor(Element element) {
        var constructor = element.getEnclosedElements()
            .stream()
            .filter(symbol -> symbol.getKind() == ElementKind.CONSTRUCTOR)
            .map(ExecutableElement.class::cast)
            .filter(executableElement -> executableElement.getParameters().isEmpty())
            .findFirst()
            .orElse(null);

        if (constructor == null) {
            print(ERROR, "Failed to a default constructor for %s", element);
        }
    }

    private void validateNoGenerics(Element element, TypeMirror elementType) {
        if (elementType instanceof DeclaredType declaredType) {
            if (!declaredType.getTypeArguments().isEmpty()) {
                print(ERROR, "Generic arguments aren't supported for auto configuration found on type %s", declaredType);
            }
        } else {
            print(ERROR, "Expected %s to be a declared type but got %s", element, elementType);
        }
    }

    private void print(Diagnostic.Kind kind, String message, Object... args) {
        processingEnv.getMessager().printMessage(kind, String.format(message, args));
    }
}
