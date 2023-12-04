package net.alphalightning.rest.handler;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import net.alphalightning.rest.RestApplication;
import net.alphalightning.rest.boundary.AlphaRestBoundary;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RestApplicationHandler {

    private static final String KEYSTORE_PASSWORD = "9aDnh2cCh7wLUt9Kv2s4";

    private static RestApplicationHandler instance;

    private final List<RestApplication> restApplications;

    private final HttpsServer httpsServer;

    private RestApplicationHandler() {
        restApplications = new LinkedList<>();
        initApiKeyHandler();
        try {
            httpsServer = HttpsServer.create(new InetSocketAddress(8000), 10);

            initSSL();

            httpsServer.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));

            httpsServer.start();
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException |
                 UnrecoverableKeyException |
                 KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private void initApiKeyHandler() {
        ApiKeyHandler.getInstance();
    }

    private void initDefaultBoundaries() {
        new AlphaRestBoundary();
    }

    private void initSSL()
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        // initialise the keystore
        char[] password = KEYSTORE_PASSWORD.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream fis = RestApplicationHandler.class.getClassLoader().getResourceAsStream("certificate.jks");
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

    public static RestApplicationHandler getInstance() {
        if(instance == null){
            instance = new RestApplicationHandler();
            instance.initDefaultBoundaries();
        }
        return instance;
    }

    public <T extends RestApplication> void registerRestApplication(T restApplication) {
        restApplications.add(restApplication);
        restApplication.init(httpsServer);
    }

}
