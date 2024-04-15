package de.linushoja.essensrest.server.handler;

import de.linushoja.essensrest.Response;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.server.cors.annotation.CORS;

import java.util.List;

public class CORSHandler {
    public static Response getResponse(List<RestMethod> restMethods, boolean authActive, CORS corsAnnotation) {
        Response response = Response.noContent();
        if (authActive) {
            response.withHeader("Access-Control-Allow-Credentials", "true");
        } else {
            response.withHeader("Access-Control-Allow-Origin", corsAnnotation.allowOrigin());
        }
        response.withHeader("Access-Control-Allow-Methods",
                restMethods.stream().map(RestMethod::toString).toArray(String[]::new));
        response.withHeader("Access-Control-Allow-Headers", "*");
        response.withHeader("Content-type", "application/json;charset=utf-8");

        return response;
    }
}
