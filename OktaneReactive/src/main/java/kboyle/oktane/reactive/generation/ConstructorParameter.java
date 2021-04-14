package kboyle.oktane.reactive.generation;

import java.lang.reflect.Type;

public record ConstructorParameter(Type type, String name, FieldMetaData field) {
}
