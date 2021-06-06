package kboyle.oktane.core.processor;

import com.google.auto.service.AutoService;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.CommandHandlerConfigurator;
import kboyle.oktane.core.exceptions.RuntimeIOException;
import kboyle.oktane.core.module.factory.PreconditionFactory;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.processor.AutoWith.GenericParameter;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javax.tools.Diagnostic.Kind.*;

@SupportedAnnotationTypes("kboyle.oktane.core.processor.AutoWith")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
@AutoService(Processor.class)
public class OktaneAutoWithProcessor extends AbstractProcessor {
    private Types types;
    private Elements elements;

    private TypeMirror typeParserType;
    private TypeMirror preconditionFactoryType;

    private Filer filer;

    private int round;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elements = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();

        var wildcard = types.getWildcardType(null, null);
        var parserBase = elements.getTypeElement(TypeParser.class.getName());
        typeParserType = types.getDeclaredType(parserBase, wildcard);
        preconditionFactoryType = types.getDeclaredType(elements.getTypeElement(PreconditionFactory.class.getName()), wildcard);

        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        print(NOTE, "");
        round++;

        if (roundEnv.processingOver() || roundEnv.errorRaised()) {
            return false;
        }

        for (var annotation : annotations) {
            for (var element : roundEnv.getElementsAnnotatedWith(annotation)) {
                try {
                    var className = "CommandHandlerWith" + getNestedPath(element);
                    var javaFileObject = filer.createSourceFile(className);

                    try (var writer = new PrintWriter(javaFileObject.openWriter())) {
                        writer.print("package ");
                        writer.print(getPackage(element));
                        writer.println(";");

                        writer.println();

                        writer.print("@");
                        writer.print(AutoService.class.getName());
                        writer.print("(");
                        writer.print(CommandHandlerConfigurator.class.getName());
                        writer.println(".class)");
                        writer.print("public final class ");
                        writer.print(className);
                        writer.print(" implements ");
                        writer.print(CommandHandlerConfigurator.class.getName());
                        writer.println(" {");

                        writer.println("\t@Override");
                        writer.print("\tpublic void apply(");
                        writer.print(CommandHandler.class.getName());
                        writer.print(".Builder");
                        writer.println(" commandHandler) {");

                        var autoWith = element.getAnnotation(AutoWith.class);
                        var genericGroups = autoWith.generics();

                        var elementType = element.asType();
                        if (types.isSubtype(elementType, typeParserType)) {
                            if (genericGroups.length == 0) {
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

                                var targetType = getTypeParserTargetType(elementType);
                                if (targetType == null) {
                                    print(ERROR, "Failed to unwrap target type for %s", element);
                                }

                                writer.print("\t\tcommandHandler.withTypeParser(");
                                writer.print(targetType);
                                writer.print(".class, new ");
                                writer.print(element);
                                writer.println("());");
                            } else {
                                for (var genericGroup : genericGroups) {
                                    var generics = genericGroup.parameters();

                                    var typeArgs = Arrays.stream(generics)
                                        .map(parameter -> elements.getTypeElement(parameter.value()))
                                        .map(TypeElement::asType)
                                        .toArray(TypeMirror[]::new);
                                    var parserType = types.getDeclaredType((TypeElement) element, typeArgs);
                                    var targetType = getTypeParserTargetType(parserType);
                                    if (targetType == null) {
                                        print(ERROR, "Failed to unwrap target type for %s", element);
                                    }

                                    var genericArgs = Arrays.stream(generics)
                                        .map(GenericParameter::value)
                                        .collect(Collectors.joining(", "));
                                    var ctorGenerics = Arrays.stream(generics)
                                        .filter(GenericParameter::passToConstructor)
                                        .map(GenericParameter::value)
                                        .map(parameter -> parameter + ".class")
                                        .collect(Collectors.joining(", "));

                                    writer.print("\t\tcommandHandler.withTypeParser(");
                                    writer.print(targetType);
                                    writer.print(".class, new ");
                                    writer.print(element);
                                    writer.print("<");
                                    writer.print(genericArgs);
                                    writer.print(">(");
                                    writer.print(ctorGenerics);
                                    writer.println("));");
                                }
                            }
                        } else if (types.isSubtype(elementType, preconditionFactoryType)) {
                            if (genericGroups.length == 0) {
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

                                writer.print("\t\tcommandHandler.withPreconditionFactory(new ");
                                writer.print(element);
                                writer.println("());");
                            } else {
                                for (var genericGroup : genericGroups) {
                                    var generics = genericGroup.parameters();;

                                    var genericArgs = Arrays.stream(generics)
                                        .map(GenericParameter::value)
                                        .collect(Collectors.joining(", "));
                                    var ctorGenerics = Arrays.stream(generics)
                                        .filter(GenericParameter::passToConstructor)
                                        .map(GenericParameter::value)
                                        .map(parameter -> parameter + ".class")
                                        .collect(Collectors.joining(", "));

                                    writer.print("\t\tcommandHandler.withPreconditionFactory(new ");
                                    writer.print(element);
                                    writer.print("<");
                                    writer.print(genericArgs);
                                    writer.print(">(");
                                    writer.print(ctorGenerics);
                                    writer.println("));");
                                }
                            }
                        } else {
                            print(WARNING, "%s is not a supported @AutoWith, skipping", element);
                        }

                        writer.println("\t}");

                        writer.println("}");
                    }
                } catch (IOException ex) {
                    throw new RuntimeIOException(ex);
                }
            }
        }

        return false;
    }

    private void print(Diagnostic.Kind kind, String message, Object... args) {
        processingEnv.getMessager().printMessage(kind, String.format(message, args));
    }

    private TypeMirror getTypeParserTargetType(TypeMirror parser) {
        return types.directSupertypes(parser).stream()
            .<TypeMirror>mapMulti(this::getTypeParserTargetType)
            .findFirst()
            .orElse(null);
    }

    private void getTypeParserTargetType(TypeMirror type, Consumer<TypeMirror> downstream) {
        if (types.isSameType(types.erasure(typeParserType), types.erasure(type)) && type instanceof DeclaredType declaredType) {
            var targetType = declaredType.getTypeArguments().get(0);
            downstream.accept(targetType);
            return;
        }

        for (var superType : types.directSupertypes(type)) {
            getTypeParserTargetType(superType, downstream);
        }
    }

    private String getNestedPath(Element element) {
        var enclosingElement = element.getEnclosingElement();
        if (enclosingElement.getKind() == ElementKind.PACKAGE) {
            return element.getSimpleName().toString();
        }

        return getNestedPath(enclosingElement) + element.getSimpleName();
    }

    private String getPackage(Element element) {
        var enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement packageElement) {
            return packageElement.toString();
        }

        return getPackage(enclosingElement);
    }
}
