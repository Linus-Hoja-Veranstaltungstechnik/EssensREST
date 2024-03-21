package de.linushoja.essensrest.client.objects;

import com.google.gson.Gson;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public record RestResponse(int code, Map<String, List<String>> headers, String body) {
    private final static Gson GSON = new Gson();

    public static RestResponse of(HttpResponse<String> httpResponse) {
        return new RestResponse(httpResponse.statusCode(), httpResponse.headers().map(), httpResponse.body());
    }

    public <T> T getBodyAsObject(Class<T> type) {
        return GSON.fromJson(body, type);
    }
}
