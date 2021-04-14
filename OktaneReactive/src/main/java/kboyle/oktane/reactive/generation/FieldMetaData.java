package kboyle.oktane.reactive.generation;

import java.lang.reflect.Type;

public record FieldMetaData(Type type, String name, AccessModifier access, boolean isFinal) {
    @Override
    public boolean equals(Object o) {
        return o instanceof FieldMetaData other && other.name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
