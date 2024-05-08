package de.linushoja.essensrest.server.handler;

import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.Headers;
import de.linushoja.essensrest.Response;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.server.cors.annotation.CORS;

import java.util.Collections;
import java.util.List;

public class CORSHandler {
    public static Response getResponse(List<RestMethod> restMethods, boolean authActive, CORS corsAnnotation, Headers requestHeaders) {
        if (!corsAnnotation.enabled()) {
            return Response.clientError();
        }

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(Collections.unmodifiableMap(requestHeaders)));

        List<String> requestedHeaders = requestHeaders.get("Access-control-request-headers");

        Response response = Response.noContent();
        response.withHeader("Access-Control-Allow-Credentials", String.valueOf(authActive));
        response.withHeader("Access-Control-Allow-Origin", corsAnnotation.allowOrigin());
        response.withHeader("Access-Control-Allow-Methods",
                restMethods.stream().map(RestMethod::toString).toArray(String[]::new));
        response.withHeader("Access-Control-Allow-Headers", requestedHeaders.toArray(String[]::new));
        response.withHeader("Content-type", "application/json;charset=utf-8");

        return response;
    }

    public static Response addCORSHeaders(Response response, boolean authActive, CORS corsAnnotation){
        if (!corsAnnotation.enabled()) {
            return response;
        }

        response.withHeader("Access-Control-Allow-Credentials", String.valueOf(authActive));
        response.withHeader("Access-Control-Allow-Origin", corsAnnotation.allowOrigin());

        return response;
    }
}
