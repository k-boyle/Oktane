package kboyle.oktane.core.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import kboyle.oktane.core.exceptions.MissingTypeParserException;
import kboyle.oktane.core.module.CommandParameter;
import kboyle.oktane.core.module.annotations.Description;
import kboyle.oktane.core.module.annotations.Name;
import kboyle.oktane.core.module.annotations.Optional;
import kboyle.oktane.core.module.annotations.Remainder;
import kboyle.oktane.core.parsers.EnumTypeParser;
import kboyle.oktane.core.parsers.TypeParser;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class CommandParameterFactory {
    private final Map<Class<?>, TypeParser<?>> typeParserByClass;

    public CommandParameterFactory(Map<Class<?>, TypeParser<?>> typeParserByClass) {
        this.typeParserByClass = new HashMap<>(typeParserByClass);
    }

    public CommandParameter.Builder createParameter(Parameter parameter) {
        var parameterType = parameter.getType();

        var parser = typeParserByClass.get(parameterType);
        if (parser == null && parameterType != String.class) {
            if (parameterType.isEnum()) {
                parser = typeParserByClass.computeIfAbsent(parameterType, type -> new EnumTypeParser(type));
            } else {
                throw new MissingTypeParserException(parameterType);
            }
        }

        var parameterBuilder = CommandParameter.builder()
            .withType(parameterType)
            .withName(parameter.getName())
            .withRemainder(parameter.getAnnotation(Remainder.class) != null)
            .withParser(parser)
            .withOriginalParameter(parameter);

        for (var annotation : parameter.getAnnotations()) {
            if (annotation instanceof Description description) {
                Preconditions.checkState(!Strings.isNullOrEmpty(description.value()), "A parameter description must be non-empty.");
                parameterBuilder.withDescription(description.value());
            } else if (annotation instanceof Name name) {
                Preconditions.checkState(!Strings.isNullOrEmpty(name.value()), "A parameter name must be non-empty.");
                parameterBuilder.withName(name.value());
            } else if (annotation instanceof Optional optional) {
                parameterBuilder.withDefaultValue(optional.defaultValue());
            }
        }
        return parameterBuilder;
    }
}
