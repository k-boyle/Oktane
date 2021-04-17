package kboyle.oktane.reactive.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import kboyle.oktane.reactive.exceptions.MissingTypeParserException;
import kboyle.oktane.reactive.module.ReactiveCommandParameter;
import kboyle.oktane.reactive.module.annotations.Description;
import kboyle.oktane.reactive.module.annotations.Name;
import kboyle.oktane.reactive.module.annotations.Remainder;
import kboyle.oktane.reactive.parsers.EnumReactiveTypeParser;
import kboyle.oktane.reactive.parsers.ReactiveTypeParser;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class CommandParameterFactory {
    private final Map<Class<?>, ReactiveTypeParser<?>> typeParserByClass;
    private final Method method;

    public CommandParameterFactory(Map<Class<?>, ReactiveTypeParser<?>> typeParserByClass, Method method) {
        this.typeParserByClass = typeParserByClass;
        this.method = method;
    }

    public ReactiveCommandParameter.Builder createParameter(Parameter parameter) {
        Class<?> parameterType = parameter.getType();

        ReactiveTypeParser<?> parser = typeParserByClass.get(parameterType);
        if (parser == null && parameterType != String.class) {
            if (parameterType.isEnum()) {
                parser = typeParserByClass.computeIfAbsent(parameterType, type -> new EnumReactiveTypeParser(type));
            } else {
                throw new MissingTypeParserException(parameterType);
            }
        }

        ReactiveCommandParameter.Builder parameterBuilder = ReactiveCommandParameter.builder()
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
