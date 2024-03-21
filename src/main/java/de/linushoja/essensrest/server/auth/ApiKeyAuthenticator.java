package de.linushoja.essensrest.server.auth;

import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.server.handler.ApiKeyHandler;
import de.linushoja.essensrest.shared.auth.AuthorizationType;

public class ApiKeyAuthenticator extends SingleValueAuthenticator {
    private static final String REALM = AuthorizationType.API_KEY.value();
    private static final String HEADER_NAME = AuthorizationType.API_KEY.getHeaderName();

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
