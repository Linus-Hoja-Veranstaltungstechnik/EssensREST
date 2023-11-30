package net.alphalightning.rest.auth;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public abstract class SingleValueAuthenticator extends Authenticator {
    protected final String realm;
    private final Charset charset;
    private final boolean isUTF8;

    public SingleValueAuthenticator(String realm) {
        this(realm, Charset.defaultCharset());
    }

    public SingleValueAuthenticator(String realm, Charset charset) {
        Objects.requireNonNull(charset);
        if (realm.isEmpty()) {
            throw new IllegalArgumentException("realm must not be empty");
        } else {
            this.realm = realm;
            this.charset = charset;
            this.isUTF8 = charset.equals(StandardCharsets.UTF_8);
        }
    }

    public String getRealm() {
        return this.realm;
    }

    public Authenticator.Result authenticate(HttpExchange t) {
        Headers rmap = t.getRequestHeaders();
        String auth = rmap.getFirst("Authorization");
        if (auth == null) {
            this.setAuthHeader(t);
            return new Authenticator.Retry(401);
        } else {
            int sp = auth.indexOf(32);
            if (sp != -1 && auth.substring(0, sp).equals("Basic")) {
                byte[] b = Base64.getDecoder().decode(auth.substring(sp + 1));
                String value = new String(b, this.charset);
                if (this.checkValue(value)) {
                    return new Authenticator.Success(new HttpPrincipal(value, this.realm));
                } else {
                    this.setAuthHeader(t);
                    return new Authenticator.Failure(401);
                }
            } else {
                return new Authenticator.Failure(401);
            }
        }
    }

    private void setAuthHeader(HttpExchange t) {
        Headers map = t.getResponseHeaders();
        String authString = "Basic realm=\"" + this.realm + "\"" + (this.isUTF8 ? ", charset=\"UTF-8\"" : "");
        map.set("WWW-Authenticate", authString);
    }

    protected abstract boolean checkValue(String value);
}
