package de.swa.gmaf;

import java.net.URI;

import javax.xml.ws.Endpoint;

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

		JdkHttpServerFactory.createHttpServer(URI.create(restApi), new GMAF_Facade_RESTImpl());
		
		System.out.println("GMAF SOAP API@: " + api);
		System.out.println("GMAF REST API@: " + restApi);
		Endpoint.publish(api, new GMAF_Facade_SOAPImpl());
			
		System.out.println("GMAF Service running for collection: " + Configuration.getInstance().getCollectionName());
	}
}
