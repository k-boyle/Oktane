package kboyle.oktane.core.processor;

import kboyle.oktane.core.module.callback.AnnotatedCommandCallback;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.io.PrintWriter;
import java.util.List;
import java.util.StringJoiner;

public class ClassWriter {
    private final Element commandModule;
    private final ExecutableElement constructor;
    private final String context;

    public ClassWriter(Element commandModule, ExecutableElement constructor, String context) {
        this.commandModule = commandModule;
        this.constructor = constructor;
        this.context = context;
    }

    public void write(PrintWriter writer, String callbackClassname, String classPackage, MethodData data) {
        var method = data.method();

        writer.print("package ");
        writer.print(classPackage);
        writer.println(";");

        writer.println();

        importClass(Mono.class, writer);
        importClass(CommandResult.class, writer);
        importClass(AnnotatedCommandCallback.class, writer);

        writer.println();

        writer.print("public class ");
        writer.print(callbackClassname);
        writer.print(" extends AnnotatedCommandCallback<");
        writer.print(context);
        writer.print(", ");
        writer.print(commandModule);
        writer.println("> {");

        writer.println("\t@Override");
        writer.println("\t@SuppressWarnings(\"unchecked\")");
        writer.print("\tpublic Mono<CommandResult> execute(");
        writer.print(commandModule);
        writer.println(" module, Object[] parameters) {");

        writer.print("\t\treturn ");

        if (!data.monoReturn()) {
            writer.print("Mono.just(");
        }

        writer.print("module.");
        writer.print(method.getSimpleName());
        writer.print("(");
        writer.print(unwrap("parameters", method.getParameters()));

        if (!data.monoReturn()) {
            writer.print(")");
        }

        writer.println(");");

        writer.println("\t}");

        writer.println();

        writer.println("\t@Override");
        writer.println("\t@SuppressWarnings(\"unchecked\")");
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

    private String unwrap(String containerName, List<? extends VariableElement> parameters) {
        var unwrapped = new StringJoiner(", ");
        for (var i = 0; i < parameters.size(); i++) {
            var parameter = parameters.get(i);
            var type = parameter.asType();
            unwrapped.add("(" + type + ") " + containerName + "[" + i + "]");
        }

        return unwrapped.toString();
    }

    private void importClass(Class<?> cl, PrintWriter writer) {
        writer.print("import ");
        writer.print(cl.getName());
        writer.println(";");
    }
}
