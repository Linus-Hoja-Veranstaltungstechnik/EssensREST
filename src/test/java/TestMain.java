import net.alphalightning.rest.auth.ApiKey;
import net.alphalightning.rest.handler.ApiKeyHandler;

public class TestMain {
    public static void main(String[] args) {
        ApiKey apiKey = ApiKeyHandler.getInstance().newApiKey("AlphaREST-Testkey");
        System.out.printf("%s : %s", apiKey.identifier(), apiKey.key());
        new TestRestApplication();
        new SecondTestRestApplication();
    }
}
