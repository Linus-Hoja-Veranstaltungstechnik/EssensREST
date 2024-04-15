package de.linushoja.essensrest.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsServer;
import de.linushoja.essensrest.Response;
import de.linushoja.essensrest.gson.GsonHelper;
import de.linushoja.essensrest.server.annotations.Auth;
import de.linushoja.essensrest.server.annotations.RestApplicationPath;
import de.linushoja.essensrest.server.auth.AuthenticatorMultiplexer;
import de.linushoja.essensrest.server.cors.annotation.CORS;
import de.linushoja.essensrest.server.handler.CORSHandler;
import de.linushoja.essensrest.server.handler.RestApplicationHandler;
import de.linushoja.essensrest.server.handler.SwaggerUiHandler;
import de.linushoja.essensrest.server.swagger.SwaggerGenerator;
import de.linushoja.essensrest.server.util.ParameterUtils;
import de.linushoja.essensrest.shared.annotations.Entity;
import de.linushoja.essensrest.shared.annotations.Path;
import de.linushoja.essensrest.shared.annotations.PathParam;
import de.linushoja.essensrest.shared.annotations.RestServerInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class RestApplication {

    private final boolean authActive;

    private final CORS corsAnnotation;

    private final ConcurrentHashMap<RestMethod, List<Method>> methods;
    private final ConcurrentHashMap<Method, String> methodPaths;

    private final AuthenticatorMultiplexer multiAuth;

    private String rootPath;

    private int httpPort = 88;
    private int httpsPort = 8888;
    private String host = "localhost";

    private HttpContext httpContext;
    private HttpContext httpsContext;

    protected RestApplication() {
        methods = new ConcurrentHashMap<>();
        methodPaths = new ConcurrentHashMap<>();

        corsAnnotation = getClass().isAnnotationPresent(CORS.class) ? getClass().getDeclaredAnnotation(CORS.class) :
                CORS.DISABLED;

        authActive = getClass().isAnnotationPresent(Auth.class);

        multiAuth = new AuthenticatorMultiplexer(authActive ?
                instantiate(getClass().getAnnotation(Auth.class).value()) : null);

        fetchServerInfo();

        RestApplicationHandler.getInstance().registerRestApplication(this);
    }

    private Authenticator[] instantiate(Class<? extends Authenticator>[] authenticators) {
        List<Authenticator> authList = new ArrayList<>();
        for (Class<? extends Authenticator> authenticatorClass : authenticators) {
            try {
                authList.add(authenticatorClass.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return authList.toArray(Authenticator[]::new);
    }

    private void fetchServerInfo() {
        RestServerInfo info = this.getClass().getAnnotation(RestServerInfo.class);
        if (info == null) return;
        httpPort = info.httpPort() != 0 ? info.httpPort() : httpPort;
        httpsPort = info.httpsPort() != 0 ? info.httpsPort() : httpsPort;
        host = info.host() != null && !info.host().isEmpty() && !info.host().isBlank() ? info.host() : host;
    }

    public void init(HttpsServer httpsServer, HttpServer httpServer, Authenticator authenticator) {
        httpContext = initApplicationContext(httpServer);
        httpContext.setAuthenticator(authenticator);
        httpsContext = initApplicationContext(httpsServer);
        httpsContext.setAuthenticator(authenticator);
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
            method.setAccessible(true);
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

    public Authenticator getMultiAuthenticator() {
        return multiAuth;
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
                if (comparePaths(fields, methodFields)) {
                    return method;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unused")
    public HttpContext getHttpContext() {
        return httpContext;
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

    @SuppressWarnings("unused")
    public HttpContext getHttpsContext() {
        return httpsContext;
    }

    private void handleConnection(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        String accept = headers.getFirst("accept");
        accept = accept == null ? headers.getFirst("accepts") : accept;
        accept = accept == null ? "" : accept;

        RestMethod requestMethod = RestMethod.valueOf(exchange.getRequestMethod());
        URI requestURI = exchange.getRequestURI();
        Response response;

        if (requestMethod == RestMethod.OPTIONS) {
            response = handleCORSRequest(requestURI);
        } else {
            response = handleRESTRequest(exchange, accept, requestMethod, requestURI);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        System.out.println("Sending response:");
        System.out.println(gson.toJson(response));

        exchange.getResponseHeaders().putAll(response.getHeaders());
        exchange.sendResponseHeaders(response.getResponseCode(),
                response.getEntity() == null || response.getEntity().isBlank() ? -1 :
                response.getEntity().getBytes(StandardCharsets.UTF_8).length);

        if (response.getEntity() != null && !response.getEntity().isBlank()) {
            OutputStream os = exchange.getResponseBody();
            os.write(response.getEntity().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
        }
    }

    private Response handleCORSRequest(URI requestURI) {
        if (!corsAnnotation.enabled()) {
            return Response.notFound();
        }

        List<RestMethod> restMethods = resolveRestMethodsByPath(requestURI.getPath());
        restMethods.add(RestMethod.OPTIONS);

        return CORSHandler.getResponse(restMethods, authActive, corsAnnotation);
    }

    private Response handleRESTRequest(HttpExchange exchange, String accept, RestMethod requestMethod, URI requestURI) {
        if (!accept.equals("application/json")) {
            return Response.clientError(Response.CLIENT_ERROR_WRONG_ACCEPT_MESSAGE);
        }

        Method method = resolveMethod(requestMethod, requestURI.getPath());

        try {
            return method != null ? callMethod(method, preparePathParams(method, requestURI.getPath()),
                    readBody(exchange.getRequestBody()), exchange.getPrincipal()) : Response.clientError(
                    "Method not Found.");
        } catch (InvocationTargetException | IllegalAccessException e) {
            return Response.serverError("Could not execute method.");
        }
    }

    private List<RestMethod> resolveRestMethodsByPath(String requestedPath) {
        List<RestMethod> result = new ArrayList<>();

        for (RestMethod restMethod : methods.keySet()) {
            if (hasPath(restMethod, requestedPath)) {
                result.add(restMethod);
            }
        }

        return result;
    }

    private Response callMethod(Method method, Map<String, String> pathParams, String entity,
                                HttpPrincipal principal) throws
                                                         InvocationTargetException,
                                                         IllegalAccessException {

        Response response;

        try {
            method.setAccessible(true);
            Parameter[] parameters = method.getParameters();
            Object[] injectedParams = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (parameter.isAnnotationPresent(PathParam.class)) {
                    String paramId = parameter.getAnnotation(PathParam.class).value();
                    injectedParams[i] = ParameterUtils.transformObject(pathParams.getOrDefault(paramId, null),
                            parameter.getType());
                    continue;
                } else if (parameter.isAnnotationPresent(Entity.class)) {
                    Object o = entity.startsWith("{") && !parameter.getType().isAssignableFrom(String.class) ?
                            GsonHelper.getGson().fromJson(entity, parameter.getType()) : entity;
                    injectedParams[i] = o;
                    continue;
                } else if (HttpPrincipal.class.isAssignableFrom(parameter.getType())) {
                    injectedParams[i] = principal;
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

    private class HttpExchangeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Handling HTTP connection from " + httpExchange.getRemoteAddress());
            System.out.println("Handling endpoint " + httpExchange.getRequestMethod() + " " + httpExchange.getRequestURI().getPath());

            handleConnection(httpExchange);
        }
    }

    private class HttpsExchangeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            HttpsExchange httpsExchange = (HttpsExchange) exchange;

            System.out.println("Handling HTTPS connection from " + httpsExchange.getRemoteAddress());
            System.out.println("Handling endpoint " + exchange.getRequestMethod() + " " + httpsExchange.getRequestURI().getPath());

            handleConnection(httpsExchange);
        }
    }
}
