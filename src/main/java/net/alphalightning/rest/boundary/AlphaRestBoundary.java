package net.alphalightning.rest.boundary;

import net.alphalightning.rest.Response;
import net.alphalightning.rest.RestApplication;
import net.alphalightning.rest.RestMethod;
import net.alphalightning.rest.annotations.*;
import net.alphalightning.rest.handler.ApiKeyHandler;

@RestApplicationPath("/alpharest")
@SuppressWarnings("unused") // rest api
public class AlphaRestBoundary extends RestApplication {

    @GET
    @Path("/auth/apikey/{app_name}/:new")
    public Response newApiKey(@PathParam("app_name") String appName) {
        return Response.ok().entity(ApiKeyHandler.getInstance().newApiKey(appName));
    }

    @PUT
    @Path("/auth/apikey/{app_name}/{api-key}")
    public Response newApiKey(@PathParam("app_name") String appName, @PathParam("api-key") String apiKey) {
        return Response.ok().entity(ApiKeyHandler.getInstance().addApiKey(appName, apiKey));
    }

    @GET
    @Path("/auth/apikey/{app_name}/callstack")
    public Response getCallstackForApiKey(@PathParam("app_name") String appName) {
        return Response.ok().entity(ApiKeyHandler.getInstance().getCallStackForApiKey(appName));
    }

    @GET
    @Path("/auth/apikey/{app_name}/call-count/{method}")
    public Response getCallCountForPathAndMethod(@PathParam("app_name") String appName, @PathParam("method") String methodString, @Entity String path) {
        return Response.ok().entity(ApiKeyHandler.getInstance().getCallCountForPathAndMethod(appName, RestMethod.valueOf(methodString), path));
    }

    @GET
    @Path("/auth/apikey/{app_name}")
    public Response getApiKey(@PathParam("app_name") String appName) {
        return Response.ok().entity(ApiKeyHandler.getInstance().getApiKey(appName));
    }

    @GET
    @Path("/auth/apikeys/")
    public Response getApiKeys() {
        return Response.ok().entity(ApiKeyHandler.getInstance().getApiKeys());
    }
}
