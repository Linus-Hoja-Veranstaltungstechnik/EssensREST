package de.linushoja.essensrest;

import de.linushoja.essensrest.gson.GsonHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Response {
    public static final int OK_RESPONSE = 200;
    public static final String OK_RESPONSE_MESSAGE = "OK";
    public static final int NO_CONTENT_RESPONSE = 204;
    public static final String NO_CONTENT_RESPONSE_MESSAGE = null;
    public static final int SERVER_ERROR_RESPONSE = 500;
    public static final String SERVER_ERROR_RESPONSE_MESSAGE = "Server Error";
    public static final int CLIENT_ERROR_RESPONSE = 400;
    public static final String CLIENT_ERROR_RESPONSE_MESSAGE = "Client Error";

    public static final int FORBIDDEN_RESPONSE = 405;

    public static final String FORBIDDEN_RESPONSE_MESSAGE = "Forbidden";

    public static final int NOT_FOUND_RESPONSE = 404;

    public static final String NOT_FOUND_RESPONSE_MESSAGE = "Not Found";
    public static final String CLIENT_ERROR_WRONG_ACCEPT_MESSAGE = "Wrong or empty accept value!";

    private final int responseCode;

    private String entity;

    private final Map<String, List<String>> headers = new HashMap<>();

    public Response(int code) {
        responseCode = code;
    }

    public static Response ok() {
        Response result = new Response(OK_RESPONSE);
        result.entity = OK_RESPONSE_MESSAGE;
        return result;
    }

    public static Response ok(String entity) {
        Response result = new Response(OK_RESPONSE);
        result.entity = entity;
        return result;
    }

    public static Response noContent() {
        Response result = new Response(NO_CONTENT_RESPONSE);
        result.entity = NO_CONTENT_RESPONSE_MESSAGE;
        return result;
    }

    public static Response forbidden() {
        Response result = new Response(FORBIDDEN_RESPONSE);
        result.entity = FORBIDDEN_RESPONSE_MESSAGE;
        return result;
    }

    public static Response forbidden(String entity) {
        Response result = new Response(FORBIDDEN_RESPONSE);
        result.entity = entity;
        return result;
    }

    public static Response notFound() {
        Response result = new Response(NOT_FOUND_RESPONSE);
        result.entity = NOT_FOUND_RESPONSE_MESSAGE;
        return result;
    }

    @SuppressWarnings("unused") // API
    public static Response notFound(String entity) {
        Response result = new Response(NOT_FOUND_RESPONSE);
        result.entity = entity;
        return result;
    }

    @SuppressWarnings("unused") // API
    public static Response clientError() {
        Response result = new Response(CLIENT_ERROR_RESPONSE);
        result.entity = CLIENT_ERROR_RESPONSE_MESSAGE;
        return result;
    }

    public static Response clientError(String entity) {
        Response result = new Response(CLIENT_ERROR_RESPONSE);
        result.entity = entity;
        return result;
    }

    public static Response error(int code){
        return new Response(code);
    }

    public static Response error(int code, String entity){
        Response result = new Response(code);
        result.entity = entity;
        return result;
    }

    @SuppressWarnings("unused") // API
    public static Response serverError() {
        Response result = new Response(SERVER_ERROR_RESPONSE);
        result.entity = SERVER_ERROR_RESPONSE_MESSAGE;
        return result;
    }

    public static Response serverError(String entity) {
        Response result = new Response(SERVER_ERROR_RESPONSE);
        result.entity = entity;
        return result;
    }

    public Response withHeader(String headerName, String... headerValue) {
        headers.put(headerName, Arrays.stream(headerValue).toList());
        return this;
    }

    public Response entity(Object entity) {
        setEntity(entity);
        return this;
    }

    private void setEntity(Object entity) {
        this.entity = GsonHelper.getGson().toJson(entity);
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getEntity() {
        return entity;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }
}
