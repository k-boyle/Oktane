package kboyle.octane.core.generation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class ConstructorGenerator implements Generator {
    private static final String CTOR_TEMPLATE = """
        %1$s %2$s(%3$s) {
            %4$s
        }
        """;

    private final ClassGenerator parent;
    private final Set<Type> imports;
    private final Map<String, ConstructorParameter> parameters;

    private AccessModifier accessModifier;

    public ConstructorGenerator(ClassGenerator parent) {
        this.parent = parent;
        this.imports = new HashSet<>();
        this.parameters = new HashMap<>();
        this.accessModifier = AccessModifier.PUBLIC;
    }

    @Override
    public Set<Type> imports() {
        return imports;
    }

    @Override
    public String generate() {
        StringJoiner constructorParameters = new StringJoiner(", ");
        StringJoiner fieldAssignments = new StringJoiner(";\n\t");

        parameters.forEach((name, parameter) -> {
            constructorParameters.add(parameter.type().getTypeName() + " " + name);
            fieldAssignments.add("this." + name + " ="  + name);
        });

        return String.format(
            CTOR_TEMPLATE,
            accessModifier.declaration(),
            parent.name,
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
