package de.linushoja.essensrest.server.handler;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebServerHandler {

    private static final String KEYSTORE_PASSWORD = "9aDnh2cCh7wLUt9Kv2s4";

    private static WebServerHandler instance;

    private final Map<Integer, HttpsServer> httpsServers;
    private final Map<Integer, HttpServer> httpServers;

    private WebServerHandler() {
        httpsServers = new HashMap<>();
        httpServers = new HashMap<>();
    }

    public static WebServerHandler getInstance() {
        if (instance == null) {
            instance = new WebServerHandler();
        }
        return instance;
    }

    public HttpServer getHttpServer(int port, boolean optional) {
        HttpServer server = httpServers.get(port);
        return server == null ? initHttpServer(port, optional) : server;
    }

    private HttpServer initHttpServer(int port, boolean optional) {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 10);

            httpServer.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));

            httpServer.start();

            httpServers.put(port, httpServer);
            return httpServer;
        } catch (BindException e) {
            if(!optional){
                throw new RuntimeException(e);
            }
            System.err.println("WARN: Optional Boundary could not be started: " + e.getMessage());
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpsServer getHttpsServer(int port, boolean optional) {
        HttpsServer server = httpsServers.get(port);
        return server == null ? initHttpsServer(port, optional) : server;
    }

    private HttpsServer initHttpsServer(int port, boolean optional) {
        try {
            HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(port), 10);

            initSSL(httpsServer);

            httpsServer.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));

            httpsServer.start();

            httpsServers.put(port, httpsServer);
            return httpsServer;
        }  catch (BindException e) {
            if(!optional){
                throw new RuntimeException(e);
            }
            System.err.println("WARN: Optional Boundary could not be started: " + e.getMessage());
            return null;
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException |
                 UnrecoverableKeyException |
                 KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private void initSSL(HttpsServer httpsServer)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        // initialise the keystore
        char[] password = KEYSTORE_PASSWORD.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream fis = WebServerHandler.class.getClassLoader().getResourceAsStream("certificate.jks");
        ks.load(fis, password);

        // setup the key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        // setup the trust manager factory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        // setup the HTTPS context and parameters
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                try {
                    // initialise the SSL context
                    SSLContext context = getSSLContext();
                    SSLEngine engine = context.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    // Set the SSL parameters
                    SSLParameters sslParameters = context.getSupportedSSLParameters();
                    params.setSSLParameters(sslParameters);

                } catch (Exception ex) {
                    System.out.println("Failed to create HTTPS port");
                }
            }
        });
    }
}
