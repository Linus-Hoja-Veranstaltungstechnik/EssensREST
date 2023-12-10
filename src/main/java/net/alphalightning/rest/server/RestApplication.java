package net.alphalightning.rest.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.*;
import net.alphalightning.rest.Response;
import net.alphalightning.rest.server.annotations.RestApplicationPath;
import net.alphalightning.rest.server.auth.ApiKeyAuthenticator;
import net.alphalightning.rest.server.handler.RestApplicationHandler;
import net.alphalightning.rest.server.handler.SwaggerUiHandler;
import net.alphalightning.rest.server.swagger.SwaggerGenerator;
import net.alphalightning.rest.server.util.ParameterUtils;
import net.alphalightning.rest.shared.annotations.Entity;
import net.alphalightning.rest.shared.annotations.Path;
import net.alphalightning.rest.shared.annotations.PathParam;
import net.alphalightning.rest.shared.annotations.RestServerInfo;

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

public abstract class RestApplication {

    private final ConcurrentHashMap<RestMethod, List<Method>> methods;
    private final ConcurrentHashMap<Method, String> methodPaths;

    private final Gson gson;

    private String rootPath;

    private int httpPort = 88;
    private int httpsPort = 8888;
    private String host = "localhost";

    private HttpContext httpContext;
    private HttpContext httpsContext;

    protected RestApplication() {
        methods = new ConcurrentHashMap<>();
        methodPaths = new ConcurrentHashMap<>();

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        gson = builder.create();

        fetchServerInfo();

        RestApplicationHandler.getInstance().registerRestApplication(this);
    }

    private void fetchServerInfo() {
        RestServerInfo info = this.getClass().getAnnotation(RestServerInfo.class);
        if (info == null) return;
        httpPort = info.httpPort() != 0 ? info.httpPort() : httpPort;
        httpsPort = info.httpsPort() != 0 ? info.httpsPort() : httpsPort;
        host = info.host() != null && !info.host().isEmpty() && !info.host().isBlank() ? info.host() : host;
    }

    public void init(HttpsServer httpsServer, HttpServer httpServer) {
        httpContext = initApplicationContext(httpServer);
        httpContext.setAuthenticator(new ApiKeyAuthenticator());
        httpsContext = initApplicationContext(httpsServer);
        httpsContext.setAuthenticator(new ApiKeyAuthenticator());
        loadMethods();
    }

    private HttpContext initApplicationContext(HttpServer server) {
        RestApplicationPath restApplicationPath = this.getClass().getAnnotation(RestApplicationPath.class);
        if (restApplicationPath == null)
            throw new RuntimeException("RestApplication needs to be annotated with RestApplicationPath.");

        rootPath = restApplicationPath.value();
        System.out.println("\n");
        System.out.println((server instanceof HttpsServer ? "HTTPS : " : "HTTP  : ") + this.getClass().getSimpleName() + " : " + rootPath);
        System.out.println(" ");

        SwaggerGenerator swaggerGenerator = new SwaggerGenerator(this);
        File swaggerFile = swaggerGenerator.generate();
        SwaggerUiHandler.register(rootPath, swaggerFile, server);
        return server.createContext(rootPath, server instanceof HttpsServer ? new HttpsExchangeHandler() : new HttpExchangeHandler());
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

    public int getHttpPort() {
        return httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
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

    public boolean hasPath(RestMethod restMethod, String path) {
        return resolveMethod(restMethod, path) != null;
    }

    private class HttpExchangeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Headers headers = httpExchange.getRequestHeaders();
            String accept = headers.getFirst("accept");
            if (!accept.equals("application/json")) return;

            System.out.println("Handling connection from " + httpExchange.getRemoteAddress());
            System.out.println("Handling endpoint " + httpExchange.getRequestURI().getPath());

            RestMethod requestMethod = RestMethod.valueOf(httpExchange.getRequestMethod());
            URI requestURI = httpExchange.getRequestURI();

            Method method = resolveMethod(requestMethod, requestURI.getPath());
            Response response;

            try {
                response = method != null ? callMethod(method, preparePathParams(method, requestURI.getPath()), readBody(httpExchange.getRequestBody())) : Response.clientError("Method not Found.");
            } catch (InvocationTargetException | IllegalAccessException e) {
                response = Response.serverError("Could not execute method.");
            }

            httpExchange.sendResponseHeaders(response.getResponseCode(), response.getEntity().getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getEntity().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
        }
    }

    private class HttpsExchangeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            HttpsExchange httpsExchange = (HttpsExchange) exchange;

            Headers headers = httpsExchange.getRequestHeaders();
            String accept = headers.getFirst("accept");
            if (!accept.equals("application/json")) return;

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
    }
}
