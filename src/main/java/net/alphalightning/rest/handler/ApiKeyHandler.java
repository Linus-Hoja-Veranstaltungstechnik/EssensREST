package net.alphalightning.rest.handler;

import net.alphalightning.rest.util.ApiKeyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApiKeyHandler {

    private static ApiKeyHandler instance;

    private List<String> apiKeys;

    private ApiKeyHandler(){
        apiKeys = new ArrayList<>();
    }

    public static ApiKeyHandler getInstance() {
        if(instance == null) instance = new ApiKeyHandler();
        return instance;
    }

    public boolean apiKeyPresent(String apiKey) {
        return apiKeys.contains(apiKey);
    }

    public String newApiKey(){
        String apiKey = ApiKeyUtils.generateApiKey();
        apiKeys.add(apiKey);
        return apiKey;
    }

}
