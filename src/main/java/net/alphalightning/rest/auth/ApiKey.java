package net.alphalightning.rest.auth;

import net.alphalightning.rest.RestMethod;
import net.alphalightning.rest.handler.ApiKeyHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record ApiKey(String identifier, String key, ConcurrentHashMap<String, Integer> callStack) {

    public ApiKey(String identifier, String key) {
        this(identifier, key, new ConcurrentHashMap<>());
    }

    public ApiKey(String identifier, String key, HashMap<String, Double> callStack) {
        this(identifier, key, transformMap(callStack));
    }

    private static ConcurrentHashMap<String, Integer> transformMap(Map<String, Double> callStack) {
        ConcurrentHashMap<String, Integer> result = new ConcurrentHashMap<>();

        for(String key : callStack.keySet()){
            Integer count = callStack.get(key).intValue();
            result.put(key, count);
        }

        return result;
    }

    public void incrementCallCount(RestMethod method, String path) {
        String key = buildKey(method, path);
        callStack.put(key, callStack.getOrDefault(key, 0) + 1);
        ApiKeyHandler.getInstance().store();
    }

    public Integer getCallCount(RestMethod method, String path) {
        String key = buildKey(method, path);
        return callStack.getOrDefault(key, 0);
    }

    private String buildKey(RestMethod method, String path) {
        return String.format("%s:%s", method.toString(), path);
    }
}
