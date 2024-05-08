package de.linushoja.essensrest.server.auth;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AuthenticatorMultiplexer extends Authenticator {
    protected List<Authenticator> authenticators;

    public AuthenticatorMultiplexer(Authenticator... authenticators) {
        if (authenticators == null) {
            return;
        }
        this.authenticators = Arrays.stream(authenticators).filter(Objects::nonNull).toList();
    }

    public Result authenticate(HttpExchange t) {
        if (authenticators.isEmpty()) {
            return new Authenticator.Success(new HttpPrincipal("none", "none"));
        }

        for (Authenticator authenticator : authenticators) {
            Result result = authenticator.authenticate(t);
            if (result.getClass().isAssignableFrom(Authenticator.Success.class)) {
                return result;
            }
        }

        return new Authenticator.Failure(401);
    }

    @SuppressWarnings("unused") // API
    public void addAuthenticator(Authenticator authenticator) {
        authenticators.add(authenticator);
    }
}
