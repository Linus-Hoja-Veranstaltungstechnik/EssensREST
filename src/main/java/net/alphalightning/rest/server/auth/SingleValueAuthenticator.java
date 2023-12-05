package net.alphalightning.rest.server.auth;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import net.alphalightning.rest.server.RestMethod;

import java.nio.charset.Charset;
import java.util.Objects;

public abstract class SingleValueAuthenticator extends Authenticator {
    protected final String realm;

    public SingleValueAuthenticator(String realm) {
        this(realm, Charset.defaultCharset());
    }

    public SingleValueAuthenticator(String realm, Charset charset) {
        Objects.requireNonNull(charset);
        if (realm.isEmpty()) {
            throw new IllegalArgumentException("realm must not be empty");
        } else {
            this.realm = realm;
        }
    }

    public Authenticator.Result authenticate(HttpExchange t) {
        Headers rmap = t.getRequestHeaders();
        RestMethod method = RestMethod.valueOf(t.getRequestMethod());
        String auth = rmap.getFirst("Authorization");
        String restPath = t.getRequestURI().getPath();
        if (auth == null) {
            this.setAuthHeader(t);
            return new Authenticator.Retry(401);
        } else {
            if (this.checkValue(auth, method, restPath)) {
                return new Authenticator.Success(new HttpPrincipal(auth, this.realm));
            } else {
                this.setAuthHeader(t);
                return new Authenticator.Failure(401);
            }
        }
    }

    private void setAuthHeader(HttpExchange t) {
        Headers map = t.getResponseHeaders();
        String authString = "Basic realm=\"" + this.realm + "\"";
        map.set("WWW-Authenticate", authString);
    }

    protected abstract boolean checkValue(String value, RestMethod method, String appName);
}
