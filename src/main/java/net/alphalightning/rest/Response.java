package net.alphalightning.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Response {
    public static final int OK_RESPONSE = 200;
    public static final String OK_RESPONSE_MESSAGE = "OK";
    public static final int SERVER_ERROR_RESPONSE = 500;
    public static final String SERVER_ERROR_RESPONSE_MESSAGE = "Server Error";
    public static final int CLIENT_ERROR_RESPONSE = 400;
    public static final String CLIENT_ERROR_RESPONSE_MESSAGE = "Client Error";

    private final int responseCode;

    private final Gson gson;

    private String entity;

    public Response(int code) {
        responseCode = code;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();

        gson = gsonBuilder.create();
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

    public Response entity(Object entity) {
        setEntity(entity);
        return this;
    }

    private void setEntity(Object entity) {
        this.entity = gson.toJson(entity);
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getEntity() {
        return entity;
    }
}
