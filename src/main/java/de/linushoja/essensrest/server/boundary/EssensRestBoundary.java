package de.linushoja.essensrest.server.boundary;

import de.linushoja.essensrest.Response;
import de.linushoja.essensrest.server.RestApplication;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.server.annotations.Auth;
import de.linushoja.essensrest.server.annotations.Optional;
import de.linushoja.essensrest.server.annotations.RestApplicationPath;
import de.linushoja.essensrest.server.auth.ApiKeyAuthenticator;
import de.linushoja.essensrest.server.cors.annotation.CORS;
import de.linushoja.essensrest.server.handler.ApiKeyHandler;
import de.linushoja.essensrest.server.swagger.annotations.Swagger;
import de.linushoja.essensrest.server.swagger.annotations.SwaggerDescription;
import de.linushoja.essensrest.server.swagger.annotations.SwaggerExample;
import de.linushoja.essensrest.server.swagger.annotations.SwaggerParameter;
import de.linushoja.essensrest.server.swagger.annotations.SwaggerResponse;
import de.linushoja.essensrest.server.swagger.annotations.SwaggerTitle;
import de.linushoja.essensrest.shared.annotations.Entity;
import de.linushoja.essensrest.shared.annotations.GET;
import de.linushoja.essensrest.shared.annotations.PUT;
import de.linushoja.essensrest.shared.annotations.Path;
import de.linushoja.essensrest.shared.annotations.PathParam;

@RestApplicationPath("/essensrest")
@Swagger
@SwaggerTitle("EssensREST")
@Optional
@CORS
@Auth(ApiKeyAuthenticator.class)
@SuppressWarnings("unused") // rest api
public class EssensRestBoundary extends RestApplication {

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
