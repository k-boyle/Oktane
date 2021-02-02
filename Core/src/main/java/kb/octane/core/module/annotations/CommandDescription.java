package kb.octane.core.module.annotations;

import kb.octane.core.module.Precondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandDescription {
    String name() default "";
    String[] aliases();
    String description() default "";
    Class<? extends Precondition>[] preconditions() default {};
    boolean synchronised() default false;
}
