package de.linushoja.essensrest.client.handler;

import de.linushoja.essensrest.client.objects.RestRequest;
import de.linushoja.essensrest.client.objects.RestResponse;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;

public class RestRequestHandler {
    public static RestResponse sendHttp(RestRequest request) {
        try {
            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest httpRequest = request.getHttpRequest();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(Charset.defaultCharset()));
            return RestResponse.of(response);
        } catch (Exception e) {
            throw new RuntimeException("Could not send Request ", e);
        }
    }

    public static RestResponse sendHttps(RestRequest request) {
        // todo
        return null;
    }
}
