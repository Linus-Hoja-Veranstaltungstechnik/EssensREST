package de.linushoja.essensrest.client.objects;

import java.net.URI;
import java.net.URISyntaxException;

public record RestRequestTarget (String host, int httpPort, int httpsPort, String restPath) {
    public URI toHttpURI() {
        try {
            return new URI(String.format("http://%s:%d/%s", host, httpPort, restPath));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not create Target URI", e);
        }
    }

    public URI toHttpsURI() {
        try {
            return new URI(String.format("https://%s:%d/%s", host, httpsPort, restPath));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not create Target URI", e);
        }
    }
}
