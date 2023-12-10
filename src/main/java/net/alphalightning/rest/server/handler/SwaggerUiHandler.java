package net.alphalightning.rest.server.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class SwaggerUiHandler implements HttpHandler {
    private static SwaggerUiHandler instance;

    private final Map<String, File> swaggerFiles;

    private final List<HttpServer> servers;

    private SwaggerUiHandler() {
        instance = this;
        servers = new LinkedList<>();
        swaggerFiles = new HashMap<>();
    }

    public static SwaggerUiHandler getInstance() {
        return instance == null ? new SwaggerUiHandler() : instance;
    }

    public static void register(String basePath, File swaggerFile, HttpServer server) {
        getInstance().swaggerFiles.put(basePath, swaggerFile);
        if (!getInstance().servers.contains(server)) getInstance().registerServer(server);
    }

    private void registerServer(HttpServer server) {
        server.createContext("/swagger", this);
        servers.add(server);
        System.out.println("created Swagger Context /swagger for server " + server.getAddress().getHostName() + ":" + server.getAddress().getPort());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers header = exchange.getResponseHeaders();

        String fullPath = exchange.getRequestURI().getPath();
        fullPath = fullPath.startsWith("/") ? fullPath.replaceFirst("/", "") : fullPath;
        String[] pathComponents = fullPath.split("/");
        String methodPath = "/" + String.join("/", Arrays.copyOfRange(pathComponents, 1, pathComponents.length - 1));
        String fileId = pathComponents[pathComponents.length - 1];
        fileId = fileId.isEmpty() || fileId.equals("ui") ? "index.html" : fileId;

        if (fileId.contains(".")) {
            String fileType = fileId.substring(fileId.lastIndexOf('.') + 1);
            switch (fileType) {
                case "js":
                    fileType = "javascript";
                case "css":
                case "html":
                case "php":
                case "map":
                case "json":
                    header.add("Content-Type", "text/" + fileType);
                default:
            }
        }

        if (!checkFile(fileId, methodPath)) {
            String response = "Error 404 File not found.";
            exchange.sendResponseHeaders(404, response.length());
            OutputStream output = exchange.getResponseBody();
            output.write(response.getBytes());
            output.flush();
            output.close();
        } else {
            exchange.sendResponseHeaders(200, 0);
            OutputStream output = exchange.getResponseBody();
            InputStream fs = getInputStream(fileId, methodPath);
            final byte[] buffer = new byte[0x10000];
            int count;
            while ((count = fs.read(buffer)) >= 0) {
                output.write(buffer, 0, count);
            }
            output.flush();
            output.close();
            fs.close();
        }
    }

    private boolean checkFile(String fileId, String path) {
        return (fileId.equals("swagger.json") && swaggerFiles.containsKey(path)) || this.getClass().getClassLoader().getResourceAsStream("swagger-ui/" + fileId) != null;
    }

    private InputStream getInputStream(String fileId, String path) {
        try {
            if (fileId.equals("swagger.json")) return new FileInputStream(swaggerFiles.get(path));
            return this.getClass().getClassLoader().getResourceAsStream("swagger-ui/" + fileId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
