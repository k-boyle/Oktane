package kboyle.oktane.core.module.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Repeatable(RequireAnyRepeatable.class)
public @interface RequireAny {
    Require[] value();
}
