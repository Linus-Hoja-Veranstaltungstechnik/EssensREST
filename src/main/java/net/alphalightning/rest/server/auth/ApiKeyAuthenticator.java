package net.alphalightning.rest.server.auth;

import net.alphalightning.rest.server.RestMethod;
import net.alphalightning.rest.server.handler.ApiKeyHandler;

public class ApiKeyAuthenticator extends SingleValueAuthenticator {
    private static final String REALM = "apikey";
    private static final String HEADER_NAME = "X-API-Key";

    public ApiKeyAuthenticator() {
        super(REALM, HEADER_NAME);
    }

    @Override
    protected boolean checkValue(String value, RestMethod method, String path) {
        try {
            if (ApiKeyHandler.getInstance().apiKeyPresent(value)) {
                String appName = ApiKeyHandler.getInstance().getAppName(value);
                ApiKeyHandler.getInstance().calledMethod(method, path, appName);
                return true;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}
