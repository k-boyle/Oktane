package com.github.kboyle.oktane.core.command;

import com.google.common.base.*;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.*;

abstract class AbstractCommandComponent implements CommandComponent {
    private final String name;
    private final String description;
    private final List<Annotation> annotations;

    protected AbstractCommandComponent(AbstractCommandComponent.Builder<?> builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.annotations = List.copyOf(builder.annotations);
    }

    @Override
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    @Override
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    @Override
    public List<Annotation> annotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .toString();
    }

    abstract static class Builder<BUILDER extends Builder<BUILDER>> {
        private final List<Annotation> annotations;

        private String name;
        private String description;

        public Builder() {
            this.annotations = new ArrayList<>();
        }

        public BUILDER name(String name) {
            Preconditions.checkState(!Strings.isNullOrEmpty(name), "name must not be null or empty");
            this.name = name;
            return self();
        }

        public BUILDER description(String description) {
            Preconditions.checkState(!Strings.isNullOrEmpty(description), "description must not be null or empty");
            this.description = description;
            return self();
        }

        public BUILDER annotation(Annotation annotation) {
            Preconditions.checkNotNull(annotation, "annotation cannot be null");
            annotations.add(annotation);
            return self();
        }

        public List<Annotation> annotations() {
            return annotations;
        }

        public String name() {
            return name;
        }

        public String description() {
            return description;
        }

        protected abstract BUILDER self();
    }
}
