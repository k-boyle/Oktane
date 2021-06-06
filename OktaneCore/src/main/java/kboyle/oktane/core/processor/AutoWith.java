package kboyle.oktane.core.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AutoWith {
    GenericParameters[] generics() default {};

    @Retention(RetentionPolicy.SOURCE)
    @interface GenericParameters {
        GenericParameter[] parameters() default {};
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface GenericParameter {
        String value();
        boolean passToConstructor() default false;
    }
}
