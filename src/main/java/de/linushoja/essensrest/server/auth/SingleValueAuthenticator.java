package de.linushoja.essensrest.server.auth;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.server.handler.RestApplicationHandler;

import java.nio.charset.Charset;
import java.util.Objects;

public abstract class SingleValueAuthenticator extends Authenticator {
    protected final String realm;
    protected final String headerName;

    public SingleValueAuthenticator(String realm, String headerName) {
        this(realm, headerName, Charset.defaultCharset());
    }

    public SingleValueAuthenticator(String realm, String headerName, Charset charset) {
        Objects.requireNonNull(charset);
        if (realm.isEmpty()) {
            throw new IllegalArgumentException("realm must not be empty");
        } else {
            this.realm = realm;
        }
        this.headerName = headerName;
    }

    public Authenticator.Result authenticate(HttpExchange t) {
        Headers rmap = t.getRequestHeaders();
        String restMethodString = t.getRequestMethod();
        if (restMethodString.equalsIgnoreCase("options")) {
            return new Authenticator.Success(new HttpPrincipal("unknown", "unknown"));
        }
        RestMethod method = RestMethod.valueOf(restMethodString);
        String auth = rmap.getFirst(headerName);
        String restPath = t.getRequestURI().getPath();
        if (!RestApplicationHandler.getInstance().restMethodExists(RestMethod.valueOf(t.getRequestMethod().toUpperCase()), restPath)) {
            return new Authenticator.Success(new HttpPrincipal("unknown", "unknown"));
        }
        if (auth == null) {
            this.setAuthHeader(t);
            return new Authenticator.Retry(401);
        }

        HttpPrincipal principal = this.checkValue(auth, method, restPath);

        if (principal != null) {
            return new Authenticator.Success(principal);
        } else {
            this.setAuthHeader(t);
            return new Authenticator.Failure(401);
        }
    }

    private void setAuthHeader(HttpExchange t) {
        Headers map = t.getResponseHeaders();
        String authString = "Basic realm=\"" + this.realm + "\"";
        map.set("WWW-Authenticate", authString);
    }

    protected abstract HttpPrincipal checkValue(String value, RestMethod method, String restPath);

    public String getRealm() {
        return realm;
    }

    public String getHeaderName() {
        return headerName;
    }
}
