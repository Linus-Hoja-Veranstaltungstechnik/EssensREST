import de.linushoja.essensrest.server.handler.ApiKeyHandler;

import java.time.Duration;

public class TestMain {
    public static void main(String[] args) throws InterruptedException {
        new TestRestApplication();
        new SecondTestRestApplication();
        new TestSwaggerDocumentation();
        Thread.sleep(Duration.ofSeconds(3).toMillis());
        new TestClient(ApiKeyHandler.getInstance().getApiKey("ADMIN").key());
    }
}
