package kboyle.oktane.core.processor;

import com.google.auto.service.AutoService;
import com.google.common.reflect.TypeToken;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.configuration.CommandHandlerConfigurator;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

class CommandHandlerWithClassBuilder {
    private final Element element;
    private final int priority;
    private final StringBuilder builder;

    CommandHandlerWithClassBuilder(Element element, String packageName, String className, int priority) {
        this.element = element;
        this.priority = priority;
        this.builder = new StringBuilder();
        appendBefore(packageName, className);
    }

    CommandHandlerWithClassBuilder appendTypeParser(TypeMirror targetType) {
        builder.append("\t\tvar targetType = ")
            .append(formatType(targetType))
            .append(";\n")
            .append("\t\tcommandHandler.withTypeParser(")
            .append("targetType, new ")
            .append(element)
            .append("());\n");

        return this;
    }

    @SuppressWarnings("UnstableApiUsage")
    private String formatType(TypeMirror type) {
        if (type instanceof DeclaredType declaredType) {
            if (declaredType.getTypeArguments().isEmpty()) {
                return declaredType + ".class";
            }

            return "(Class<" + declaredType + ">) new " + TypeToken.class.getName() + "<" + declaredType + ">() {}.getRawType()";
        }

        return type + ".class";
    }

    CommandHandlerWithClassBuilder appendPreconditionFactory() {
        builder.append("\t\tcommandHandler.withPreconditionFactory(new ")
            .append(element)
            .append("());\n");

        return this;
    }

    private void appendBefore(String packageName, String className) {
        builder.append("package ");
        builder.append(packageName);
        builder.append(";\n");

        builder.append("\n");

        builder.append("@");
        builder.append(AutoService.class.getName());
        builder.append("(");
        builder.append(CommandHandlerConfigurator.class.getName());
        builder.append(".class)\n");
        builder.append("public final class ");
        builder.append(className);
        builder.append(" implements ");
        builder.append(CommandHandlerConfigurator.class.getName());
        builder.append(" {\n");

        builder.append("\t@Override\n");
        builder.append("\tpublic void apply(");
        builder.append(CommandHandler.class.getName());
        builder.append(".Builder<?> commandHandler) {\n");
    }

    private void appendAfter() {
        builder.append("\t}\n");

        builder.append("\n");

        builder.append("\t@Override\n");
        builder.append("\tpublic int priority() {\n");
        builder.append("\t\treturn ");
        builder.append(priority);
        builder.append(";\n");
        builder.append("\t}\n");

        builder.append("}\n");
    }

    @Override
    public String toString() {
        appendAfter();
        return builder.toString();
    }
}
