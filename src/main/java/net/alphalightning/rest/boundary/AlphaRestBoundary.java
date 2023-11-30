package net.alphalightning.rest.boundary;

import net.alphalightning.rest.Response;
import net.alphalightning.rest.RestApplication;
import net.alphalightning.rest.annotations.GET;
import net.alphalightning.rest.annotations.Path;
import net.alphalightning.rest.annotations.RestApplicationPath;
import net.alphalightning.rest.handler.ApiKeyHandler;

@RestApplicationPath("/alpharest")
public class AlphaRestBoundary extends RestApplication {

    @GET
    @Path("/auth/apikey/:new")
    public Response newApiKey() {
        return Response.ok(ApiKeyHandler.getInstance().newApiKey());
    }
}
