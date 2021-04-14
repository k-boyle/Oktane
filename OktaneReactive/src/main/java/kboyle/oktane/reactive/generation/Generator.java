package kboyle.oktane.reactive.generation;

import java.lang.reflect.Type;
import java.util.Set;

public interface Generator {
    Set<Type> imports();
    String generate();
}
