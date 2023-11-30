package net.alphalightning.rest.handler;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import net.alphalightning.rest.RestApplication;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RestApplicationHandler {
    private static final String KEYSTORE_PASSWORD = "9aDnh2cCh7wLUt9Kv2s4";
    private static final String SERVICE_RUNNING_RESPONSE = "Service Running";

    private static RestApplicationHandler instance;

    private final List<RestApplication> restApplications;

    private final HttpsServer httpsServer;

    private RestApplicationHandler() {
        restApplications = new LinkedList<>();
        try {
            httpsServer = HttpsServer.create(new InetSocketAddress(8000), 10);
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

            httpsServer.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));

            httpsServer.start();

            httpsServer.createContext("/serviceup", ex -> {
                HttpsExchange sex = (HttpsExchange) ex;
                System.out.println(sex.getRemoteAddress());
                sex.sendResponseHeaders(200, SERVICE_RUNNING_RESPONSE.getBytes(StandardCharsets.UTF_8).length);
                OutputStream os = sex.getResponseBody();
                os.write(SERVICE_RUNNING_RESPONSE.getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();
            });
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException |
                 UnrecoverableKeyException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public static RestApplicationHandler getInstance() {
        return instance == null ? instance = new RestApplicationHandler() : instance;
    }

    public <T extends RestApplication> void registerRestApplication(T restApplication) {
        restApplications.add(restApplication);
        restApplication.init(httpsServer);
    }

}
