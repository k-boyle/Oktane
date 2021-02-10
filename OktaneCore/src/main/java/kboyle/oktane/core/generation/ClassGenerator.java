package kboyle.oktane.core.generation;

import com.google.common.base.Preconditions;
import kboyle.oktane.core.exceptions.UnhandledTypeException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static kboyle.oktane.core.generation.GenerationUtil.formatType;

public class ClassGenerator implements Generator {
    private static final String IMPORT_TEMPLATE = "import %s.%s;";
    private static final String FIELD_TEMPLATE = "%s final %s %s;";

    private final Set<Type> imports;
    private final Set<Type> interfaces;
    private final Map<String, FieldMetaData> fields;
    private final ConstructorGenerator constructorGenerator;
    private final List<MethodGenerator> methods;

    String name;
    private String clazzPackage;
    private AccessModifier accessModifier;
    private Type baseClazz;

    public ClassGenerator() {
        this.imports = new HashSet<>();
        this.interfaces = new HashSet<>();
        this.fields = new LinkedHashMap<>();
        this.accessModifier = AccessModifier.PUBLIC;
        this.constructorGenerator = new ConstructorGenerator(this);
        this.methods = new ArrayList<>();
    }

    public ClassGenerator withPackage(String classPackage) {
        this.clazzPackage = classPackage;
        return this;
    }

    @Override
    public Set<Type> imports() {
        Set<Type> aggregatedTypes = new HashSet<>(imports);
        fields.values().stream().map(FieldMetaData::type).forEach(aggregatedTypes::add);
        aggregatedTypes.addAll(constructorGenerator.imports());
        methods.stream().map(MethodGenerator::imports).forEach(aggregatedTypes::addAll);
        return aggregatedTypes;
    }

    @Override
    public String generate() {
        StringBuilder classBuilder = new StringBuilder()
            .append("package ")
            .append(clazzPackage)
            .append(";");

        StringBuilder importStatements = new StringBuilder();

        Set<Type> imports = imports();
        for (Type type : imports) {
            importClazzes(importStatements, imports, type);
        }

        classBuilder.append(importStatements.toString());

        StringJoiner classSignature = new StringJoiner(" ");
        classSignature.add(accessModifier.declaration());
        classSignature.add("class");
        classSignature.add(name);

        if (baseClazz != null) {
            classSignature.add("extends");
            classSignature.add(formatType(baseClazz));
        }

        if (!interfaces.isEmpty()) {
            classSignature.add("implements");
            StringJoiner implemented = new StringJoiner(", ");
            interfaces.forEach(type -> implemented.add(formatType(type)));
            classSignature.add(implemented.toString());
        }

        classSignature.add("{");

        classBuilder.append(classSignature.toString());


        StringBuilder fieldDeclarations = new StringBuilder();

        for (FieldMetaData field : fields.values()) {
            fieldDeclarations.append(String.format(
                FIELD_TEMPLATE,
                field.access().declaration(),
                formatType(field.type()),
                field.name()
            ));
        }

        classBuilder.append(fieldDeclarations.toString());
        classBuilder.append(constructorGenerator.generate());

        for (MethodGenerator method : methods) {
            classBuilder.append(method.generate());
        }

        classBuilder.append("}");
        return classBuilder.toString();
    }

    private void handleParameterizedType(StringBuilder importStatements, Set<Type> imports, ParameterizedType parameterizedType) {
        Type rawType = parameterizedType.getRawType();
        if (!imports.contains(rawType)) {
            importClazzes(importStatements, rawType);
        }

        for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
            if (!imports.contains(typeArgument)) {
                importClazzes(importStatements, imports, typeArgument);
            }
        }
    }

    private void importClazzes(StringBuilder importStatements, Set<Type> imports, Type typeArgument) {
        if (typeArgument instanceof ParameterizedType argumentParameterizedType) {
            handleParameterizedType(importStatements, imports, argumentParameterizedType);
        } else {
            importClazzes(importStatements, typeArgument);
        }
    }

    private void importClazzes(StringBuilder importStatements, Type typeArgument) {
        if (typeArgument instanceof Class<?> clazz) {
            if (clazz.isPrimitive()) {
                return;
            }

            if (clazz.isArray()) {
                importStatements.append(importClazz(clazz.getComponentType()));
            } else {
                importStatements.append(importClazz(clazz));
            }
        } else {
            throw new UnhandledTypeException(typeArgument);
        }
    }

    private String importClazz(Class<?> clazz) {
        return String.format(IMPORT_TEMPLATE, clazz.getPackageName(), clazz.getSimpleName());
    }

    public ClassGenerator withName(String name) {
        this.name = name;
        return this;
    }

    public ClassGenerator withAccess(AccessModifier accessModifier) {
        Preconditions.checkState(accessModifier == AccessModifier.PACKAGE_PRIVATE || accessModifier == AccessModifier.PUBLIC);
        this.accessModifier = accessModifier;
        return this;
    }

    public ClassGenerator withExtension(Type base) {
        this.imports.add(base);
        this.baseClazz = base;
        return this;
    }

    public ClassGenerator withImplementation(Type implementation) {
        this.imports.add(implementation);
        this.interfaces.add(implementation);
        return this;
    }

    public ClassGenerator withField(Type type, String name, AccessModifier access, boolean isFinal) {
        this.imports.add(type);
        FieldMetaData field = new FieldMetaData(type, name, access, isFinal);
        this.fields.put(name, field);
        this.constructorGenerator.withParameter(field);
        return this;
    }

    public ClassGenerator withMethod(MethodGenerator method) {
        this.methods.add(method);
        return this;
    }
}
