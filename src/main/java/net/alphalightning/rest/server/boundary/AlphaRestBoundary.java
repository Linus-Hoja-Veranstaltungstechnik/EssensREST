package net.alphalightning.rest.server.boundary;

import net.alphalightning.rest.Response;
import net.alphalightning.rest.server.RestApplication;
import net.alphalightning.rest.server.RestMethod;
import net.alphalightning.rest.server.annotations.RestApplicationPath;
import net.alphalightning.rest.server.handler.ApiKeyHandler;
import net.alphalightning.rest.server.swagger.annotations.*;
import net.alphalightning.rest.shared.annotations.*;

@RestApplicationPath("/alpharest")
@SwaggerTitle("AlphaREST")
@SuppressWarnings("unused") // rest api
public class AlphaRestBoundary extends RestApplication {

    @GET
    @Path("/auth/apikey/{app-name}/:new")
    @SwaggerDescription("Creates and returns a new trusted api-key for a given app name.")
    @SwaggerResponse(description = "Api-key created.")
    public Response newApiKey(@PathParam("app-name") @SwaggerParameter(description = "Name of the app which the api key is generated for") String appName) {
        return Response.ok().entity(ApiKeyHandler.getInstance().newApiKey(appName));
    }

    @PUT
    @Path("/auth/apikey/{app-name}/{api-key}")
    @SwaggerDescription("Adds a api-key to the trusted keys for a given app name.")
    @SwaggerResponse(description = "Api-key trusted.")
    public Response newApiKey(@PathParam("app-name") @SwaggerParameter(description = "Name of the app which the api key is trusted for") String appName, @PathParam("api-key") @SwaggerParameter(description = "Api-key which should be trusted") String apiKey) {
        return Response.ok().entity(ApiKeyHandler.getInstance().addApiKey(appName, apiKey));
    }

    @GET
    @Path("/auth/apikey/{app-name}/callstack")
    public Response getCallstackForApiKey(@PathParam("app-name") String appName) {
        return Response.ok().entity(ApiKeyHandler.getInstance().getCallStackForApiKey(appName));
    }

    @GET
    @Path("/auth/apikey/{app-name}/call-count/{method}")
    public Response getCallCountForPathAndMethod(@PathParam("app-name") String appName, @PathParam("method") RestMethod method, @Entity(name = "path") String path) {
        return Response.ok().entity(ApiKeyHandler.getInstance().getCallCountForPathAndMethod(appName, method, path));
    }

    @GET
    @Path("/auth/apikey/{app-name}")
    public Response getApiKey(@PathParam("app-name") String appName) {
        return Response.ok().entity(ApiKeyHandler.getInstance().getApiKey(appName));
    }

    @GET
    @Path("/auth/apikeys")
    public Response getApiKeys() {
        return Response.ok().entity(ApiKeyHandler.getInstance().getApiKeys());
    }

    @PUT
    @Path("/echo")
    public Response echo(@Entity(name = "echoString") @SwaggerExample("echo this") String echoString) {
        return Response.ok(echoString);
    }
}
