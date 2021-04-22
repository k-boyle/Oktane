package kboyle.oktane.reactive.processor;

import com.google.auto.service.AutoService;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.module.callback.AnnotatedCommandCallback;
import kboyle.oktane.reactive.results.command.CommandResult;
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
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedAnnotationTypes("kboyle.oktane.reactive.processor.OktaneModule")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
@AutoService(Processor.class)
public class OktaneModuleProcessor extends AbstractProcessor {
    private Types types;

    private TypeMirror moduleBaseType;
    private TypeMirror commandReturnType;

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.types = processingEnv.getTypeUtils();
        Elements elements = processingEnv.getElementUtils();

        TypeElement moduleBase = elements.getTypeElement(ReactiveModuleBase.class.getName());
        TypeElement context = elements.getTypeElement(CommandContext.class.getName());
        WildcardType baseGeneric = types.getWildcardType(context.asType(), null);
        this.moduleBaseType = types.getDeclaredType(moduleBase, baseGeneric);

        TypeMirror commandResult = elements.getTypeElement(CommandResult.class.getName()).asType();
        TypeElement mono = elements.getTypeElement(Mono.class.getName());
        this.commandReturnType = types.getDeclaredType(mono, commandResult);

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

                ExecutableElement constructor = getConstructor(commandModule);
                commandModule.getEnclosedElements().stream()
                    .filter(ExecutableElement.class::isInstance)
                    .map(ExecutableElement.class::cast)
                    .filter(element -> element.getModifiers().contains(Modifier.PUBLIC))
                    .filter(element -> types.isSameType(element.getReturnType(), commandReturnType))
                    .forEach(method -> {
                        String callbackClassname = getGeneratedClassName(commandModule, method);

                        try {
                            JavaFileObject javaFileObject = filer.createSourceFile(callbackClassname);

                            TypeMirror contextType = getContextType(commandModule);
                            if (contextType == null) {
                                print(ERROR, "Failed to unwrap context type for %s", moduleType);
                                return;
                            }

                            String context = contextType.toString();

                            try (PrintWriter writer = new PrintWriter(javaFileObject.openWriter())) {
                                writer.println("package kboyle.oktane.reactive.processor;");

                                writer.println();

                                writer.print("import ");
                                writer.print(Mono.class.getName());
                                writer.println(";");
                                writer.print("import ");
                                writer.print(CommandResult.class.getName());
                                writer.println(";");
                                writer.print("import ");
                                writer.print(AnnotatedCommandCallback.class.getName());
                                writer.println(";");

                                writer.println();

                                writer.print("public class ");
                                writer.print(callbackClassname);
                                writer.print(" extends AnnotatedCommandCallback<");
                                writer.print(context);
                                writer.print(", ");
                                writer.print(commandModule);
                                writer.println("> {");

                                writer.println("\t@Override");
                                writer.print("\tpublic Mono<CommandResult> execute(");
                                writer.print(commandModule);
                                writer.println(" module, Object[] parameters) {");
                                writer.print("\t\treturn module.");
                                writer.print(method.getSimpleName());
                                writer.print("(");
                                writer.print(unwrap("parameters", method.getParameters()));
                                writer.println(");");
                                writer.println("\t}");

                                writer.println();

                                writer.println("\t@Override");
                                writer.print("\tpublic ");
                                writer.print(commandModule);
                                writer.println(" getModule(Object[] beans) {");
                                writer.print("\t\treturn new ");
                                writer.print(commandModule);
                                writer.print("(");
                                writer.print(unwrap("beans", constructor.getParameters()));
                                writer.println(");");
                                writer.println("\t}");

                                writer.print("}");
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
                    });
            }
        }

        print(NOTE, "");
        return true;
    }

    private void print(Diagnostic.Kind kind, String message, Object... args) {
        processingEnv.getMessager().printMessage(kind, String.format(message, args));
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

    private String getNestedPath(Element commandModule) {
        Element enclosingElement = commandModule.getEnclosingElement();
        if (enclosingElement.getKind() == ElementKind.PACKAGE) {
            return commandModule.getSimpleName().toString();
        }

        return getNestedPath(enclosingElement) + "$$" + commandModule.getSimpleName();
    }

    private String unwrap(String containerName, List<? extends VariableElement> parameters) {
        StringJoiner unwrapped = new StringJoiner(", ");
        for (int i = 0; i < parameters.size(); i++) {
            VariableElement parameter = parameters.get(i);
            TypeMirror type = parameter.asType();
            unwrapped.add("(" + type + ") " + containerName + "[" + i + "]");
        }

        return unwrapped.toString();
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
}
