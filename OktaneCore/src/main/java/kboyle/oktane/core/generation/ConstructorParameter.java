package kboyle.oktane.core.generation;

import java.lang.reflect.Type;

public record ConstructorParameter(Type type, String name, FieldMetaData field) {
}
