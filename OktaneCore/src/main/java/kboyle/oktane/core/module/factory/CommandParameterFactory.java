package kboyle.oktane.core.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import kboyle.oktane.core.exceptions.MissingTypeParserException;
import kboyle.oktane.core.module.CommandParameter;
import kboyle.oktane.core.module.annotations.Description;
import kboyle.oktane.core.module.annotations.Name;
import kboyle.oktane.core.module.annotations.Remainder;
import kboyle.oktane.core.parsers.EnumTypeParser;
import kboyle.oktane.core.parsers.TypeParser;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class CommandParameterFactory {
    private final Map<Class<?>, TypeParser<?>> typeParserByClass;
    private final Method method;

    public CommandParameterFactory(Map<Class<?>, TypeParser<?>> typeParserByClass, Method method) {
        this.typeParserByClass = typeParserByClass;
        this.method = method;
    }

    public CommandParameter.Builder createParameter(Parameter parameter) {
        Class<?> parameterType = parameter.getType();

        TypeParser<?> parser = typeParserByClass.get(parameterType);
        if (parser == null && parameterType != String.class) {
            if (parameterType.isEnum()) {
                parser = typeParserByClass.computeIfAbsent(parameterType, type -> new EnumTypeParser(type));
            } else {
                throw new MissingTypeParserException(parameterType);
            }
        }

        CommandParameter.Builder parameterBuilder = CommandParameter.builder()
            .withType(parameterType)
            .withName(parameter.getName())
            .withRemainder(parameter.getAnnotation(Remainder.class) != null)
            .withParser(parser);

        Description parameterDescription = method.getAnnotation(Description.class);
        if (parameterDescription != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(parameterDescription.value()), "A parameter description must be non-empty.");
            parameterBuilder.withDescription(parameterDescription.value());
        }

        Name parameterName = parameter.getAnnotation(Name.class);
        if (parameterName != null) {
            Preconditions.checkState(!Strings.isNullOrEmpty(parameterName.value()), "A parameter name must be non-empty.");
            parameterBuilder.withName(parameterName.value());
        }

        return parameterBuilder;
    }
}
