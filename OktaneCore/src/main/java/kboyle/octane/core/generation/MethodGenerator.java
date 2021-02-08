package kboyle.octane.core.generation;

import com.google.common.base.Strings;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class MethodGenerator implements Generator {
    private static int methodCounter = 0;

    private final Set<Type> imports;
    private final Map<String, MethodParameter> parameters;

    private String name;
    private Type returnType;
    private AccessModifier accessModifier;
    private boolean synchronised;
    // todo figure out how to cleanly implement this properly
    private String body;

    public MethodGenerator() {
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
        StringJoiner methodSignature = new StringJoiner(" ");
        methodSignature.add(accessModifier.declaration());

        if (synchronised) {
            methodSignature.add("synchronized");
        }

        if (returnType != null) {
            methodSignature.add(returnType.getTypeName());
        } else {
            methodSignature.add("void");
        }

        if (name != null && !Strings.isNullOrEmpty(name)) {
            methodSignature.add(name);
        } else {
            methodSignature.add("method" + methodCounter++);
        }

        StringJoiner methodArguments = new StringJoiner(", ", "(", ")");

        parameters.forEach((name, parameter) -> methodArguments.add(parameter.type().getTypeName() + " name"));

        methodSignature.add(methodArguments.toString());

        StringJoiner formattedBody = new StringJoiner("\n\t\t", "\n{", "\n}");

        for (String loc : body.split("\n")) {
            formattedBody.add(loc);
        }

        return methodSignature.toString() + formattedBody.toString();
    }

    public MethodGenerator withName(String name) {
        this.name = name;
        return this;
    }

    public MethodGenerator withReturn(Type returnType) {
        this.imports.add(returnType);
        this.returnType = returnType;
        return this;
    }

    public MethodGenerator withAccess(AccessModifier accessModifier) {
        this.accessModifier = accessModifier;
        return this;
    }

    public MethodGenerator withParameter(Type type, String name) {
        this.imports.add(type);
        this.parameters.put(name, new MethodParameter(type, name));
        return this;
    }

    public MethodGenerator synchronised() {
        this.synchronised = true;
        return this;
    }

    public MethodGenerator withSynchronised(boolean synchronised) {
        this.synchronised = synchronised;
        return this;
    }

    public MethodGenerator withBody(Set<Type> imports, String body) {
        this.imports.addAll(imports);
        this.body = body;
        return this;
    }
}
