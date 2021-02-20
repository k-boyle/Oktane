package kboyle.oktane.core.generation;

import java.lang.reflect.Type;
import java.util.*;

import static kboyle.oktane.core.generation.GenerationUtil.formatType;

public class ConstructorGenerator implements Generator {
    private static final String CTOR_TEMPLATE = "%1$s %2$s(%3$s) {%4$s}";

    private final String name;
    private final Set<Type> imports;
    private final Map<String, ConstructorParameter> parameters;

    private AccessModifier accessModifier;

    public ConstructorGenerator(String name) {
        this.name = name;
        this.imports = new HashSet<>();
        this.parameters = new LinkedHashMap<>();
        this.accessModifier = AccessModifier.PUBLIC;
    }

    @Override
    public Set<Type> imports() {
        return imports;
    }

    // todo return stream of lines
    @Override
    public String generate() {
        StringJoiner constructorParameters = new StringJoiner(", ");
        StringJoiner fieldAssignments = new StringJoiner(";", "", ";");

        parameters.forEach((name, parameter) -> {
            constructorParameters.add(formatType(parameter.type()) + " " + name);
            fieldAssignments.add("this." + name + " = "  + name);
        });

        return String.format(
            CTOR_TEMPLATE,
            accessModifier.declaration(),
            name,
            constructorParameters.toString(),
            fieldAssignments.toString()
        );
    }

    public ConstructorGenerator withParameter(FieldMetaData field) {
        this.parameters.put(field.name(), new ConstructorParameter(field.type(), field.name(), field));
        return this;
    }

    public ConstructorGenerator withAccess(AccessModifier accessModifier) {
        this.accessModifier = accessModifier;
        return this;
    }
}
