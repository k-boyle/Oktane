package kboyle.octane.core.generation;

import java.lang.reflect.Type;
import java.util.Set;

public interface Generator {
    Set<Type> imports();
    String generate();
}
