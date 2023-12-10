package net.alphalightning.rest.server.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.alphalightning.rest.server.RestMethod;
import net.alphalightning.rest.server.util.ApiKeyUtils;
import net.alphalightning.rest.shared.auth.ApiKey;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class ApiKeyHandler {

    private static final File APIKEY_FILE = new File("alpharest/apikeys.json");
    private static final File CALLSTACK_FILE = new File("alpharest/callstacks.json");

    private static ApiKeyHandler instance;

    private ConcurrentHashMap<String, ApiKey> apiKeys;

    private final Gson gson;

    private ApiKeyHandler() {
        apiKeys = new ConcurrentHashMap<>();

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        gson = builder.create();

        loadApiKeysFromLocalStorage();
        storeAdminKeyIfNoKeyExisting();
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

    public boolean apiKeyPresent(ApiKey apiKey) {
        return apiKeys.containsValue(apiKey);
    }

    public ApiKey newApiKey(String appName) {
        loadApiKeysFromLocalStorage();
        ApiKey result = apiKeys.getOrDefault(appName, apiKeys.put(appName, ApiKeyUtils.generateApiKey(appName)));
        storeApiKeysToLocalStorage();
        return result;
    }

    public ApiKey getApiKey(String appName) {
        loadApiKeysFromLocalStorage();
        return apiKeys.get(appName);
    }

    public List<ApiKey> getApiKeys() {
        loadApiKeysFromLocalStorage();
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
        return apiKeyPresent(apiKey) ? getApiKey(getAppName(apiKey)) : putApiKey(appName, new ApiKey(appName, apiKey));
    }

    public ApiKey addApiKey(ApiKey apiKey) {
        return apiKeyPresent(apiKey) ? getApiKey(getAppName(apiKey.identifier())) : putApiKey(apiKey.identifier(), apiKey);
    }

    private ApiKey putApiKey(String appName, ApiKey apiKey) {
        apiKeys.put(appName, apiKey);
        storeApiKeysToLocalStorage();
        return apiKey;
    }

    private void storeApiKeysToLocalStorage() {
        try {
            if (!APIKEY_FILE.exists()) {
                APIKEY_FILE.getParentFile().mkdirs();
                APIKEY_FILE.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileWriter fileWriter = new FileWriter(APIKEY_FILE)) {
            gson.toJson(mapToApiKeyHashMap(apiKeys), fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        storeCallStacks();
    }

    private void storeAdminKeyIfNoKeyExisting() {
        if (!apiKeys.isEmpty()) return;
        newApiKey("ADMIN");
        storeApiKeysToLocalStorage();
        storeCallStacks();
    }

    private void loadApiKeysFromLocalStorage() {
        if (!APIKEY_FILE.exists()) return;

        try (FileReader fileReader = new FileReader(APIKEY_FILE)) {
            apiKeys = mapToApiKeys(gson.fromJson(fileReader, HashMap.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loadCallstacks();
    }

    private void loadCallstacks() {
        if (!CALLSTACK_FILE.exists()) return;

        try (FileReader fileReader = new FileReader(CALLSTACK_FILE)) {
            mapToCallstacks(gson.fromJson(fileReader, HashMap.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void mapToCallstacks(HashMap<String, ConcurrentHashMap<String, Double>> fromJson) {
        for (String appName : fromJson.keySet()) {
            Map<String, Double> callStack = fromJson.get(appName);
            ApiKey oldApiKey = apiKeys.get(appName);

            apiKeys.put(appName, new ApiKey(appName, oldApiKey.key(), new HashMap<>(callStack)));
        }
    }

    private void storeCallStacks() {
        try {
            if (!CALLSTACK_FILE.exists()) {
                CALLSTACK_FILE.getParentFile().mkdirs();
                CALLSTACK_FILE.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileWriter fileWriter = new FileWriter(CALLSTACK_FILE)) {
            gson.toJson(mapCallstacksToHashMap(), fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<String, ConcurrentHashMap<String, Integer>> mapCallstacksToHashMap() {
        HashMap<String, ConcurrentHashMap<String, Integer>> result = new HashMap<>();

        for (ApiKey apiKey : apiKeys.values()) {
            result.put(apiKey.identifier(), apiKey.callStack());
        }

        return result;
    }

    private ConcurrentHashMap<String, ApiKey> mapToApiKeys(HashMap<String, String> fromJson) {
        ConcurrentHashMap<String, ApiKey> result = new ConcurrentHashMap<>();

        for (String appName : fromJson.keySet()) {
            String apiKey = fromJson.get(appName);
            result.put(appName, new ApiKey(appName, apiKey, apiKeys.getOrDefault(appName, new ApiKey("", "")).callStack()));
        }

        return result;
    }

    private HashMap<String, String> mapToApiKeyHashMap(ConcurrentHashMap<String, ApiKey> entity) {
        HashMap<String, String> result = new HashMap<>();

        for (ApiKey apiKey : entity.values()) {
            result.put(apiKey.identifier(), apiKey.key());
        }

        return result;
    }

    public void store() {
        storeApiKeysToLocalStorage();
    }
}
