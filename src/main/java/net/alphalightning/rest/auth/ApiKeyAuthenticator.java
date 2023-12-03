package net.alphalightning.rest.auth;

import net.alphalightning.rest.RestMethod;
import net.alphalightning.rest.handler.ApiKeyHandler;

public class ApiKeyAuthenticator extends SingleValueAuthenticator {
    private static final String REALM = "apikey";

    public ApiKeyAuthenticator() {
        super(REALM);
    }

    @Override
    protected boolean checkValue(String value, RestMethod method, String path) {
        if (ApiKeyHandler.getInstance().apiKeyPresent(value)) {
            String appName = ApiKeyHandler.getInstance().getAppName(value);
            ApiKeyHandler.getInstance().calledMethod(method, path, appName);
            return true;
        }
        return false;
    }

}
