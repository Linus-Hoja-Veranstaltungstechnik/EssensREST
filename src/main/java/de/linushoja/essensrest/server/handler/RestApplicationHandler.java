package de.linushoja.essensrest.server.handler;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import de.linushoja.essensrest.server.RestApplication;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.server.annotations.Optional;
import de.linushoja.essensrest.server.boundary.EssensRestBoundary;

import java.util.LinkedList;
import java.util.List;

public class RestApplicationHandler {
    private static RestApplicationHandler instance;

    private final List<RestApplication> restApplications;

    private RestApplicationHandler() {
        restApplications = new LinkedList<>();
        initApiKeyHandler();

    }

    private void initApiKeyHandler() {
        ApiKeyHandler.getInstance();
    }

    private void initDefaultBoundaries() {
        new EssensRestBoundary();
    }

    public static RestApplicationHandler getInstance() {
        if (instance == null) {
            instance = new RestApplicationHandler();
            instance.initDefaultBoundaries();
        }
        return instance;
    }

    public <T extends RestApplication> void registerRestApplication(T restApplication) {
        restApplications.add(restApplication);
        int httpPort = restApplication.getHttpPort();
        int httpsPort = restApplication.getHttpsPort();
        HttpServer httpServer = WebServerHandler.getInstance().getHttpServer(httpPort,
                restApplication.getClass().isAnnotationPresent(Optional.class));
        HttpsServer httpsServer = WebServerHandler.getInstance().getHttpsServer(httpsPort,
                restApplication.getClass().isAnnotationPresent(Optional.class));
        if (httpsServer != null && httpServer != null) {
            restApplication.init(httpsServer, httpServer, restApplication.getMultiAuthenticator());
        }
    }

    public boolean restMethodExists(RestMethod restMethod, String method) {
        for (RestApplication app : restApplications) {
            if (app.hasPath(restMethod, method)) return true;
        }
        return false;
    }

}
