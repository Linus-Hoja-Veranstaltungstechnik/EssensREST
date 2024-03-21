package net.alphalightning.rest.client.handler;

import net.alphalightning.rest.client.objects.RestRequest;
import net.alphalightning.rest.client.objects.RestResponse;

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
