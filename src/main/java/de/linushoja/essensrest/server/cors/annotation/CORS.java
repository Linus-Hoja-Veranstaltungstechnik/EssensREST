package de.linushoja.essensrest.server.cors.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CORS {
    String ORIGIN_ANY = "*";
    CORS DISABLED = new CORS() {

        @Override
        public Class<? extends Annotation> annotationType() {
            return CORS.class;
        }

        @Override
        public boolean enabled() {
            return false;
        }

        @Override
        public String allowOrigin() {
            return ORIGIN_ANY;
        }
    };

    boolean enabled() default true;

    String allowOrigin() default ORIGIN_ANY;
}
