package net.alphalightning.rest.handler;

import net.alphalightning.rest.RestMethod;
import net.alphalightning.rest.auth.ApiKey;
import net.alphalightning.rest.util.ApiKeyUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ApiKeyHandler {

    private static ApiKeyHandler instance;

    private final ConcurrentHashMap<String, ApiKey> apiKeys;

    private ApiKeyHandler() {
        apiKeys = new ConcurrentHashMap<>();
    }

    public static ApiKeyHandler getInstance() {
        if (instance == null) instance = new ApiKeyHandler();
        return instance;
    }

    public boolean apiKeyPresent(String apiKey) {
        for (ApiKey key : apiKeys.values()) {
            if (key.key().equals(apiKey)) return true;
        }
        return false;
    }

    public ApiKey newApiKey(String appName) {
        return apiKeys.getOrDefault(appName, apiKeys.put(appName, ApiKeyUtils.generateApiKey(appName)));
    }

    public ApiKey getApiKey(String appName) {
        return apiKeys.get(appName);
    }

    public List<ApiKey> getApiKeys() {
        return apiKeys.values().stream().toList();
    }

    public ConcurrentHashMap<String, Integer> getCallStackForApiKey(String appName) {
        return getApiKey(appName).callStack();
    }

    public Integer getCallCountForPathAndMethod(String path, RestMethod method, String appName) {
        return getApiKey(appName).getCallCount(method, path);
    }

    public void calledMethod(RestMethod method, String path, String appName) {
        getApiKey(appName).incrementCallCount(method, path);
    }

    public String getAppName(String value) {
        for (ApiKey apiKey : apiKeys.values()) {
            if (apiKey.key().equals(value)) return apiKey.identifier();
        }
        return null;
    }

    public ApiKey addApiKey(String appName, String apiKey) {
        return apiKeyPresent(apiKey) ? getApiKey(getAppName(apiKey)) : apiKeys.put(appName, new ApiKey(appName, apiKey));
    }
}
