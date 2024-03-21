package net.alphalightning.rest.client.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.alphalightning.rest.client.handler.RestRequestHandler;
import net.alphalightning.rest.server.RestMethod;
import net.alphalightning.rest.shared.auth.AuthorizationType;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record RestRequest(RestRequestTarget target, RestMethod restMethod, Map<String, List<String>> headers,
                          String body) {

    public static final String APPLICATION_JSON = "application/json";

    public static RestRequestBuilder builder(RestRequestTarget target) {
        return RestRequestBuilder.newRequest(target);
    }

    public static class RestRequestBuilder {
        private final RestRequestTarget target;

        private final Gson gson;
        private RestMethod restMethod = RestMethod.GET;
        private Map<String, List<String>> headers = new HashMap<>();
        private String body = "";

        private RestRequestBuilder(RestRequestTarget target) {
            GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization();
            gson = gsonBuilder.create();

            this.target = target;
        }

        private static RestRequestBuilder newRequest(RestRequestTarget target) {
            return new RestRequestBuilder(target);
        }

        public RestRequestBuilder method(RestMethod method) {
            this.restMethod = method;
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
            this.body = gson.toJson(entity);
            return this;
        }

        public RestRequestBuilder withAuthorization(AuthorizationType authType, String authString) {
            withHeader(authType.getHeaderName(), authString);
            return this;
        }

        public RestRequest build() {
            return new RestRequest(target, restMethod, headers, body);
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
        httpRequestBuilder.uri(target.toHttpURI());
        //httpRequestBuilder.version(HttpClient.Version.HTTP_1_1);
        headers.keySet().stream().forEach(key -> headers.get(key).forEach(value -> {
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
