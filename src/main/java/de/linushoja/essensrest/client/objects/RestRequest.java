package de.linushoja.essensrest.client.objects;

import de.linushoja.essensrest.gson.GsonHelper;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.client.handler.RestRequestHandler;
import de.linushoja.essensrest.shared.auth.AuthorizationType;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record RestRequest(RestRequestTarget target, RestMethod restMethod, Map<String, List<String>> headers,
                          String body, String restPath) {

    public static final String APPLICATION_JSON = "application/json";

    public static RestRequestBuilder builder(RestRequestTarget target) {
        return RestRequestBuilder.newRequest(target);
    }

    public static class RestRequestBuilder {
        private final RestRequestTarget target;
        private RestMethod restMethod = RestMethod.GET;
        private final Map<String, List<String>> headers = new HashMap<>();
        private String body = "";
        private String restPath = "";

        private RestRequestBuilder(RestRequestTarget target) {
            this.target = target;
        }

        private static RestRequestBuilder newRequest(RestRequestTarget target) {
            return new RestRequestBuilder(target);
        }

        public RestRequestBuilder method(RestMethod method) {
            this.restMethod = method;
            return this;
        }

        public RestRequestBuilder path(String restPath) {
            this.restPath = restPath;
            return this;
        }

        public RestRequestBuilder withHeaders(Map<String, List<String>> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public RestRequestBuilder withHeader(String key, String value) {
            this.headers.put(key, headers.getOrDefault(key, new ArrayList<>()));
            this.headers.get(key).add(value);
            return this;
        }

        public RestRequestBuilder accepts(String acceptType) {
            return withHeader("accept", acceptType);
        }

        public RestRequestBuilder withBody(String body) {
            this.body = body;
            return withHeader("Content-Type", APPLICATION_JSON);
        }

        public RestRequestBuilder withEntity(Object entity) {
            this.body = GsonHelper.getGson().toJson(entity);
            return this;
        }

        public RestRequestBuilder withAuthorization(AuthorizationType authType, String authString) {
            withHeader(authType.getHeaderName(), authString);
            return this;
        }

        public RestRequest build() {
            return new RestRequest(target, restMethod, headers, body, restPath);
        }
    }

    public RestResponse sendHttp() {
        return RestRequestHandler.sendHttp(this);
    }

    public RestResponse sendHttps() {
        return RestRequestHandler.sendHttps(this);
    }

    public HttpRequest getHttpRequest() {
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        httpRequestBuilder.uri(target.toHttpURI(restPath));
        headers.keySet().forEach(key -> headers.get(key).forEach(value -> {
            if (value != null) httpRequestBuilder.header(key, value);
        }));
        switch (restMethod) {
            case GET -> httpRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
            case PUT -> httpRequestBuilder.method("PUT", HttpRequest.BodyPublishers.ofString(body));
            case DELETE -> httpRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
            case POST -> httpRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofString(body));
        }
        httpRequestBuilder.timeout(Duration.of(10, ChronoUnit.SECONDS));
        return httpRequestBuilder.build();
    }
}
