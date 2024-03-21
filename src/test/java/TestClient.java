import net.alphalightning.rest.client.objects.RestRequest;
import net.alphalightning.rest.client.objects.RestRequestTarget;
import net.alphalightning.rest.server.RestMethod;
import net.alphalightning.rest.shared.auth.AuthorizationType;

import static net.alphalightning.rest.client.objects.RestRequest.APPLICATION_JSON;

public class TestClient {
    public TestClient(String apiKey) {
        RestRequest request = RestRequest.builder(new RestRequestTarget("localhost", 88, 8888, "alpharest/echo"))
                .withAuthorization(AuthorizationType.API_KEY, apiKey)
                .method(RestMethod.PUT)
                .accepts(APPLICATION_JSON)
                .withBody("TEST")
                .build();
        assert "TEST".equals(request.sendHttp().body());
    }
}
