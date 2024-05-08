import de.linushoja.essensrest.Response;
import de.linushoja.essensrest.server.RestApplication;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.server.annotations.Auth;
import de.linushoja.essensrest.server.annotations.RestApplicationPath;
import de.linushoja.essensrest.server.auth.ApiKeyAuthenticator;
import de.linushoja.essensrest.server.swagger.annotations.SwaggerExample;
import de.linushoja.essensrest.shared.annotations.DELETE;
import de.linushoja.essensrest.shared.annotations.Entity;
import de.linushoja.essensrest.shared.annotations.GET;
import de.linushoja.essensrest.shared.annotations.POST;
import de.linushoja.essensrest.shared.annotations.PUT;
import de.linushoja.essensrest.shared.annotations.Path;
import de.linushoja.essensrest.shared.annotations.PathParam;

@RestApplicationPath("/essensrest/test")
@Auth(ApiKeyAuthenticator.class)
@SuppressWarnings("unused")
public class TestRestApplication extends RestApplication {

    @Path("/ping")
    @GET
    public Response ping() {
        return Response.ok("Service online");
    }

    @Path("/get")
    @GET
    public Response get() {
        return Response.ok().entity(HundeService.getAll());
    }

    @Path("/put/{id}")
    @PUT
    public Response put(@PathParam("id") String id, @Entity(name = "hund") @SwaggerExample("{\n  \"name\": \"Bello\",\n  \"age\": 3\n}") Hund hund) {
        HundeService.put(id, hund);
        return Response.ok("Hund gespeichert.");
    }

    @Path("/get/{id}")
    @GET
    public Response get(@PathParam("id") String id) {
        return Response.ok().entity(HundeService.get(id));
    }

    @Path("/post")
    @POST
    public Response post() {
        return Response.ok("post");
    }

    @Path("/delete")
    @DELETE
    public Response delete() {
        return Response.ok("delete");
    }

    @Path("/add/{value1}/{value2}")
    @GET
    public Response add(@PathParam("value1") Double value1, @PathParam("value2") Double value2) {
        return Response.ok("" + (value1 + value2));
    }

    @Path("/enum/{enum}")
    @GET
    public Response add(@PathParam("enum") RestMethod restMethod) {
        return Response.ok().entity(restMethod);
    }

}
