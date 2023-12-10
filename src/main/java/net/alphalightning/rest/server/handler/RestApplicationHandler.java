package net.alphalightning.rest.server.handler;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import net.alphalightning.rest.server.RestApplication;
import net.alphalightning.rest.server.RestMethod;
import net.alphalightning.rest.server.boundary.AlphaRestBoundary;

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
        new AlphaRestBoundary();
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
        HttpServer httpServer = WebServerHandler.getInstance().getHttpServer(httpPort);
        HttpsServer httpsServer = WebServerHandler.getInstance().getHttpsServer(httpsPort);
        restApplication.init(httpsServer, httpServer);
    }

    public boolean restMethodExists(RestMethod restMethod, String method) {
        for (RestApplication app : restApplications) {
            if (app.hasPath(restMethod, method)) return true;
        }
        return false;
    }

}
