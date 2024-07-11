package de.swa.gmaf;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.xml.ws.Endpoint;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;

import de.swa.gmaf.api.GMAF_Facade_RESTImpl;
import de.swa.gmaf.api.GMAF_Facade_SOAPImpl;
import de.swa.ui.AutoProcessThread;
import de.swa.ui.Configuration;

public class Service {
	public static void main(String[] args) {
		Service.start();
	}

	public static void start() {
		// if auto processing is activated, check for MMFGs
		if (Configuration.getInstance().isAutoProcess()) {
			AutoProcessThread at = new AutoProcessThread();
			at.setDaemon(true);
			at.start();
		}

		String api = "http://" + Configuration.getInstance().getServerName() + ":"
				+ Configuration.getInstance().getServerPort() + "/" + Configuration.getInstance().getContext()
				+ "/gmafApi";

		String restApi = "http://" + Configuration.getInstance().getServerName() + ":"
				+ Configuration.getInstance().getRestServicePort() + "/" + Configuration.getInstance().getContext()
				+ "/gmafApi";

		GMAF_Facade_RESTImpl resourceConfig = new GMAF_Facade_RESTImpl();
		resourceConfig.register(new CORSFilter());
		JdkHttpServerFactory.createHttpServer(URI.create(restApi), resourceConfig);

		System.out.println("GMAF SOAP API@: " + api);
		System.out.println("GMAF REST API@: " + restApi);
		Endpoint.publish(api, new GMAF_Facade_SOAPImpl());
			
		System.out.println("GMAF Service running for collection: " + Configuration.getInstance().getCollectionName());
	}

	private static class CORSFilter implements ContainerResponseFilter {
		@Override
		public void filter(ContainerRequestContext request,
						   ContainerResponseContext response) throws IOException {
			response.getHeaders().add("Access-Control-Allow-Origin", "*");
			response.getHeaders().add("Access-Control-Allow-Headers",
					"CSRF-Token, X-Requested-By, Authorization, Content-Type");
			response.getHeaders().add("Access-Control-Allow-Credentials", "true");
			response.getHeaders().add("Access-Control-Allow-Methods",
					"GET, POST, PUT, DELETE, OPTIONS, HEAD");
		}
	}
}
