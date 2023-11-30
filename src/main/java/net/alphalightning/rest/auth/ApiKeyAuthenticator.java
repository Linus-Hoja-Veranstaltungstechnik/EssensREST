package net.alphalightning.rest.auth;

public class ApiKeyAuthenticator extends SingleValueAuthenticator {
    private static final String REALM = "apikey";

    public ApiKeyAuthenticator() {
        super(REALM);
    }

    @Override
    protected boolean checkValue(String value) {
        return false;
    }

}
