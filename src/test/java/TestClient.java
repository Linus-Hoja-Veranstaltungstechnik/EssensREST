import de.linushoja.essensrest.client.objects.RestRequest;
import de.linushoja.essensrest.client.objects.RestRequestTarget;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.shared.auth.AuthorizationType;

import static de.linushoja.essensrest.client.objects.RestRequest.APPLICATION_JSON;

public class TestClient {
    public TestClient(String apiKey) {
        RestRequest request = RestRequest.builder(new RestRequestTarget("localhost", 88, 8888, "essensrest/echo"))
                .withAuthorization(AuthorizationType.API_KEY, apiKey)
                .method(RestMethod.PUT)
                .accepts(APPLICATION_JSON)
                .withBody("TEST")
                .build();
        assert "TEST".equals(request.sendHttp().body());
    }
}
