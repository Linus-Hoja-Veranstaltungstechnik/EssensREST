package de.linushoja.essensrest.server.annotations;

import de.linushoja.essensrest.server.auth.SingleValueAuthenticator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {
    Class<? extends SingleValueAuthenticator>[] value();
}
