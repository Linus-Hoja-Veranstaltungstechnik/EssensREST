package net.alphalightning.rest.server.swagger.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SwaggerResponses.class)
public @interface SwaggerResponse {
    int code() default 200;

    String description() default "OK";
}
