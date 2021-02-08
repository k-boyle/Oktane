package kboyle.octane.core.generation;

import com.google.common.base.Preconditions;
import kboyle.octane.core.exceptions.UnhandledTypeException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class ClassGenerator implements Generator {
    private static final String IMPORT_TEMPLATE = "import %s.%s;";
    private static final String GENERIC_TEMPLATE = "%s<%s>";

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
        this.fields = new HashMap<>();
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
        StringBuilder classBuilder = new StringBuilder();
        classBuilder.append(clazzPackage);
        classBuilder.append("\n");

        StringJoiner importStatements = new StringJoiner("\n");

        Set<Type> imports = imports();
        for (Type type : imports) {
            if (type instanceof ParameterizedType parameterizedType) {
                handleParameterizedType(importStatements, imports, parameterizedType);
            } else if (type instanceof Class<?> clazz) {
                if (clazz.isPrimitive()) {
                    continue;
                }

                importStatements.add(importClazz(clazz));
            } else {
                throw new UnhandledTypeException(type);
            }
        }

        classBuilder.append(importStatements.toString());
        classBuilder.append("\n");

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
        classBuilder.append("\n");





        classBuilder.append("\n");
        classBuilder.append("}");
        return classBuilder.toString();
    }

    private void handleParameterizedType(StringJoiner importStatements, Set<Type> imports, ParameterizedType parameterizedType) {
        Type rawType = parameterizedType.getRawType();
        if (!imports.contains(rawType)) {
            if (rawType instanceof Class<?> clazz) {
                importStatements.add(importClazz(clazz));
            } else {
                throw new UnhandledTypeException(rawType);
            }
        }

        for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
            if (!imports.contains(typeArgument)) {
                if (typeArgument instanceof ParameterizedType argumentParameterizedType) {
                    handleParameterizedType(importStatements, imports, argumentParameterizedType);
                } else if (typeArgument instanceof Class<?> clazz) {
                    importStatements.add(importClazz(clazz));
                } else {
                    throw new UnhandledTypeException(typeArgument);
                }
            }
        }
    }

    private String importClazz(Class<?> clazz) {
        return String.format(IMPORT_TEMPLATE, clazz.getPackageName(), clazz.getSimpleName());
    }

    private String formatType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz.getSimpleName();
        } else if (type instanceof ParameterizedType parameterizedType) {
            StringJoiner generics = new StringJoiner(", ");
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            for (Type typeArgument : typeArguments) {
                generics.add(formatType(typeArgument));
            }

            Type rawType = parameterizedType.getRawType();
            return String.format(GENERIC_TEMPLATE, rawType.getTypeName(), generics.toString());
        } else {
            throw new UnhandledTypeException(type);
        }
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


/*

package p;

imports;

public class n extends e implements x, y, z {
    private final Something something;

    public n(Something something) {
        this.something = something;
    }

    public synchronized T m(String arg1, int arg2) {

    }
}


 */
