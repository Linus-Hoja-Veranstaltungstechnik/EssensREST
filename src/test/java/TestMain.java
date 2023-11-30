import net.alphalightning.rest.handler.ApiKeyHandler;

public class TestMain {
    public static void main(String[] args) {
        System.out.println(ApiKeyHandler.getInstance().newApiKey());
        new TestRestApplication();
        new SecondTestRestApplication();
    }
}
