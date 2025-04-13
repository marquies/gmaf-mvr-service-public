package de.swa.gmaf;

import java.io.IOException;
import java.net.URI;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.xml.ws.Endpoint;

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import de.swa.gmaf.api.GMAF_Facade_RESTImpl;
import de.swa.gmaf.api.GMAF_Facade_SOAPImpl;
import de.swa.ui.AutoProcessThread;
import de.swa.ui.Configuration;

public class Service {
    private static boolean running = false;
    private static HttpServer restServer;
    private static Endpoint soapEndpoint;

    public static void main(String[] args) {
        new Service().start();
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        // if auto processing is activated, check for MMFGs
        if (Configuration.getInstance().isAutoProcess()) {
            AutoProcessThread at = new AutoProcessThread();
            at.setDaemon(true);
            at.start();
        }



        String api = "http://" + Configuration.getInstance().getServerName() + ":"
                + Configuration.getInstance().getServerPort() + "/" + Configuration.getInstance().getContext()
                + "/gmafApi";

        String url = "http://" + Configuration.getInstance().getServerName() + ":"
                + Configuration.getInstance().getRestServicePort();
        String restApi = url + "/" + Configuration.getInstance().getContext()
                + "/gmafApi";

        Api apiConfig = new Api();
        apiConfig.register(new CORSFilter());

        restServer = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(restApi),
                apiConfig
        );
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        CLStaticHttpHandler swaggerUiHandler = new CLStaticHttpHandler(cl, "/META-INF/resources/webjars/swagger-ui/5.20.7/");
        restServer.getServerConfiguration().addHttpHandler(swaggerUiHandler, "/swagger-ui");

        System.out.println("GMAF SOAP API@: " + api);
        System.out.println("GMAF REST API@: " + restApi);
        System.out.println("Swagger UI available at: " + url + "/swagger-ui/index.html and the OpenAPI at: " + url + "/gmaf/gmafApi/openapi.json");

        soapEndpoint = Endpoint.publish(api, new GMAF_Facade_SOAPImpl());
        // Start auto processing if enabled

        running = true;
        System.out.println("GMAF Service running for collection: " + Configuration.getInstance().getCollectionName());

    }

    public synchronized void stop() {
        try {
            if (restServer != null) {
                restServer.stop();
                restServer = null;
            }

            if (soapEndpoint != null) {
                soapEndpoint.stop();
                soapEndpoint = null;
            }

            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class CORSFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext request,
                         ContainerResponseContext response) throws IOException {
            response.getHeaders().add("Access-Control-Allow-Origin", "*");
            response.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
            response.getHeaders().add("Access-Control-Allow-Credentials", "true");
            response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        }
    }
}