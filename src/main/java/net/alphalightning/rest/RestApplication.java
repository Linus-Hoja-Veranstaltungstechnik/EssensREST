package net.alphalightning.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsServer;
import net.alphalightning.rest.annotations.Entity;
import net.alphalightning.rest.annotations.Path;
import net.alphalightning.rest.annotations.PathParam;
import net.alphalightning.rest.annotations.RestApplicationPath;
import net.alphalightning.rest.auth.ApiKeyAuthenticator;
import net.alphalightning.rest.handler.RestApplicationHandler;
import net.alphalightning.rest.util.ParameterUtils;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class RestApplication implements HttpHandler {

    private final ConcurrentHashMap<RestMethod, List<Method>> methods;
    private final ConcurrentHashMap<Method, String> methodPaths;

    private final Gson gson;

    private String rootPath;

    private HttpContext httpContext;

    protected RestApplication() {
        methods = new ConcurrentHashMap<>();
        methodPaths = new ConcurrentHashMap<>();

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        gson = builder.create();

        RestApplicationHandler.getInstance().registerRestApplication(this);
    }

    public void init(HttpsServer server) {
        httpContext = initApplicationContext(server);
        httpContext.setAuthenticator(new ApiKeyAuthenticator());
        loadMethods();
    }

    private HttpContext initApplicationContext(HttpsServer server) {
        RestApplicationPath restApplicationPath = this.getClass().getAnnotation(RestApplicationPath.class);
        if (restApplicationPath == null)
            throw new RuntimeException("RestApplication needs to be annotated with RestApplicationPath.");

        rootPath = restApplicationPath.value();
        System.out.println("\n");
        System.out.println(this.getClass().getSimpleName() + " : " + rootPath);
        System.out.println(" ");

        return server.createContext(rootPath, this);
    }

    private void loadMethods() {
        for (Method method : this.getClass().getMethods()) {
            RestMethod restMethod = null;
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                restMethod = RestMethod.getMethodByAnnotation(annotation.annotationType());
                if (restMethod != null) break;
            }
            if (restMethod != null) {
                if (!method.isAnnotationPresent(Path.class)) continue;

                String methodPath = (rootPath + method.getAnnotation(Path.class).value()).replace("//", "/");

                System.out.printf("%-6s : %-60s --> %s#%s%n", restMethod, methodPath, getClass().getCanonicalName(), method.getName());

                methodPaths.put(method, methodPath);

                List<Method> methodsOfRestMethod = methods.getOrDefault(restMethod, new ArrayList<>());
                methodsOfRestMethod.add(method);
                if (!methods.containsKey(restMethod)) methods.put(restMethod, methodsOfRestMethod);
            }
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpsExchange httpsExchange = (HttpsExchange) exchange;

        System.out.println("Handling connection from " + httpsExchange.getRemoteAddress());
        System.out.println("Handling endpoint " + httpsExchange.getRequestURI().getPath());

        RestMethod requestMethod = RestMethod.valueOf(httpsExchange.getRequestMethod());
        URI requestURI = httpsExchange.getRequestURI();

        Method method = resolveMethod(requestMethod, requestURI.getPath());
        Response response;

        try {
            response = method != null ? callMethod(method, preparePathParams(method, requestURI.getPath()), readBody(httpsExchange.getRequestBody())) : Response.clientError("Method not Found.");
        } catch (InvocationTargetException | IllegalAccessException e) {
            response = Response.serverError("Could not execute method.");
        }

        httpsExchange.sendResponseHeaders(response.getResponseCode(), response.getEntity().getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = httpsExchange.getResponseBody();
        os.write(response.getEntity().getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();
    }

    private String readBody(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    private Map<String, String> preparePathParams(Method method, String path) {
        Map<String, String> resolvedParams = new HashMap<>();

        String[] fields = parsePath(path);
        String[] methodFields = parsePath(methodPaths.get(method));

        for (int i = 0; i < fields.length; i++) {
            if (methodFields[i].startsWith("{") && methodFields[i].endsWith("}")) {
                String paramId = methodFields[i].replace("{", "").replace("}", "");
                resolvedParams.put(paramId, fields[i]);
            }
        }

        return resolvedParams;
    }

    private Response callMethod(Method method, Map<String, String> pathParams, String entity) throws InvocationTargetException, IllegalAccessException {

        Response response;

        try {
            method.setAccessible(true);
            Parameter[] parameters = method.getParameters();
            Object[] injectedParams = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (parameter.isAnnotationPresent(PathParam.class)) {
                    String paramId = parameter.getAnnotation(PathParam.class).value();
                    injectedParams[i] = ParameterUtils.transformObject(pathParams.getOrDefault(paramId, null), parameter.getType());
                    continue;
                } else if (parameter.isAnnotationPresent(Entity.class)) {
                    Object o = gson.fromJson(entity, parameter.getType());
                    injectedParams[i] = o;
                    continue;
                }
                injectedParams[i] = null;
            }

            if (injectedParams.length == 0) {
                response = (Response) method.invoke(this);
            } else {
                response = (Response) method.invoke(this, injectedParams);
            }
        } catch (Exception e) {
            response = Response.serverError("Error occured during method execution: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }

    private Method resolveMethod(RestMethod requestMethod, String requestedPath) {
        String[] fields = parsePath(requestedPath);
        int fieldCount = fields.length;
        List<Method> possibleMethods = methods.get(requestMethod);
        possibleMethods.sort((m, om) -> paramCount(methodPaths.get(m)) - paramCount(methodPaths.get(om)));

        for (Method method : possibleMethods) {
            String methodPath = methodPaths.get(method);
            String[] methodFields = parsePath(methodPath);
            if (fieldCount == methodFields.length) {
                if (comparePaths(fields, methodFields)) return method;
            }
        }

        return null;
    }

    private int paramCount(String s) {
        String[] fields = parsePath(s);

        int count = 0;

        for (String field : fields) {
            if(field.startsWith("{") && field.endsWith("}")) count++;
        }
        return count;
    }

    private boolean comparePaths(String[] fields, String[] methodFields) {
        for (int i = 0; i < fields.length; i++) {
            if (methodFields[i].startsWith("{") && methodFields[i].endsWith("}")) {
                continue;
            }
            if (fields[i].equals(methodFields[i])) {
                continue;
            }
            return false;
        }

        return true;
    }

    private String[] parsePath(String path) {
        return Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }
}
