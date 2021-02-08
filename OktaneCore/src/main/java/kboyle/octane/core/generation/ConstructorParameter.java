package kboyle.octane.core.generation;

import java.lang.reflect.Type;

public record ConstructorParameter(Type type, String name, FieldMetaData field) {
}
