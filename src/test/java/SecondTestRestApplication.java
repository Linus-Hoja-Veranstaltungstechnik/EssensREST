import de.linushoja.essensrest.Response;
import de.linushoja.essensrest.server.RestApplication;
import de.linushoja.essensrest.server.annotations.Auth;
import de.linushoja.essensrest.server.annotations.RestApplicationPath;
import de.linushoja.essensrest.server.auth.ApiKeyAuthenticator;
import de.linushoja.essensrest.shared.annotations.DELETE;
import de.linushoja.essensrest.shared.annotations.Entity;
import de.linushoja.essensrest.shared.annotations.GET;
import de.linushoja.essensrest.shared.annotations.POST;
import de.linushoja.essensrest.shared.annotations.PUT;
import de.linushoja.essensrest.shared.annotations.Path;
import de.linushoja.essensrest.shared.annotations.PathParam;

@RestApplicationPath("/essensrest/test2")
@Auth(ApiKeyAuthenticator.class)
@SuppressWarnings("unused")
public class SecondTestRestApplication extends RestApplication {

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
    public Response put(@PathParam("id") String id, @Entity(name = "hund") Hund hund) {
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

}
