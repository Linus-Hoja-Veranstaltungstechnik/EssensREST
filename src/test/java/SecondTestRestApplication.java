import net.alphalightning.rest.Response;
import net.alphalightning.rest.server.RestApplication;
import net.alphalightning.rest.server.annotations.RestApplicationPath;
import net.alphalightning.rest.shared.annotations.*;

@RestApplicationPath("/alpharest/test2")
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
    public Response put(@PathParam("id") String id, @Entity() Hund hund) {
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

    @Path("/patch")
    @PATCH
    public Response patch() {
        return Response.ok("patch");
    }

}
