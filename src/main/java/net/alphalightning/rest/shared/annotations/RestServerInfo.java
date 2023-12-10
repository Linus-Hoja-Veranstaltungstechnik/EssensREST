package net.alphalightning.rest.shared.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestServerInfo {
    String host() default "localhost";

    int httpPort() default 88;

    int httpsPort() default 8888;
}
