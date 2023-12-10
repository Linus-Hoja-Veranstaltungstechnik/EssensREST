package net.alphalightning.rest.server.swagger.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SwaggerParameter {
    String description() default "";

    String operationId() default "";

    boolean required() default true;

    long min() default Long.MIN_VALUE;

    long max() default Long.MAX_VALUE;

    String[] possibleValues() default {};
}
