package com.github.kboyle.oktane.core.command;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public interface CommandComponent {
    Optional<String> name();
    Optional<String> description();
    List<Annotation> annotations();
}
