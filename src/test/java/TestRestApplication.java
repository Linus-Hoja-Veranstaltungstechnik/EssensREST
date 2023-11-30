import net.alphalightning.rest.Response;
import net.alphalightning.rest.RestApplication;
import net.alphalightning.rest.annotations.*;

@RestApplicationPath("/alpharest/test")
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

    @Path("/add/{value1}/{value2}")
    @GET
    public Response add(@PathParam("value1") Double value1, @PathParam("value2") Double value2) {
        return Response.ok("" + (value1 + value2));
    }

}
