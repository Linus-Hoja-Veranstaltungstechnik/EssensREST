package net.alphalightning.rest.auth;

import net.alphalightning.rest.RestMethod;

import java.util.concurrent.ConcurrentHashMap;

public record ApiKey(String identifier, String key, ConcurrentHashMap<String, Integer> callStack) {

    public ApiKey(String identifier, String key) {
        this(identifier, key, new ConcurrentHashMap<>());
    }

    public void incrementCallCount(RestMethod method, String path) {
        String key = buildKey(method, path);
        callStack.put(key, callStack.getOrDefault(key, 0) + 1);
    }

    public Integer getCallCount(RestMethod method, String path) {
        String key = buildKey(method, path);
        return callStack.getOrDefault(key, 0);
    }

    private String buildKey(RestMethod method, String path) {
        return String.format("%s:%s", method.toString(), path);
    }
}
