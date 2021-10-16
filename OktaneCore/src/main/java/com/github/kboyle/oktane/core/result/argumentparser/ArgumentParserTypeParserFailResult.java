package com.github.kboyle.oktane.core.result.argumentparser;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.result.FailResult;
import com.github.kboyle.oktane.core.result.typeparser.TypeParserResult;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ArgumentParserTypeParserFailResult<T> implements ArgumentParserResult, FailResult {
    private final CommandParameter<T> parameter;
    private final String token;
    private final TypeParserResult<T> typeParserResult;

    public ArgumentParserTypeParserFailResult(CommandParameter<T> parameter, String token, TypeParserResult<T> typeParserResult) {
        this.parameter = Preconditions.checkNotNull(parameter, "parameter cannot be null");
        this.token = Preconditions.checkNotNull(token, "token cannot be null");
        this.typeParserResult = Preconditions.checkNotNull(typeParserResult, "typeParserResult cannot be null");
    }

    public CommandParameter<T> parameter() {
        return parameter;
    }

    public String token() {
        return token;
    }

    public TypeParserResult<?> typeParserResult() {
        return typeParserResult;
    }

    @Override
    public String failureReason() {
        return String.format("Argument parsing failed due to not being able to parse %s as %s", token, parameter.type());
    }

    @Override
    public Object[] parsedArguments() {
        return new Object[0];
    }
}
