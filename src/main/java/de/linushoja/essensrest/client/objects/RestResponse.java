package de.linushoja.essensrest.client.objects;

import de.linushoja.essensrest.gson.GsonHelper;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public record RestResponse(int code, Map<String, List<String>> headers, String body) {

    public static RestResponse of(HttpResponse<String> httpResponse) {
        return new RestResponse(httpResponse.statusCode(), httpResponse.headers().map(), httpResponse.body());
    }

    public <T> T getBodyAsObject(Class<T> type) {
        return GsonHelper.getGson().fromJson(body, type);
    }
}
