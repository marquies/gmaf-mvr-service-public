package de.swa.gmaf;

import de.swa.gmaf.api.GMAF_Facade_RESTImpl;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import java.util.List;
import java.util.Set;

/**
 * Created by Patrick Steinert on 13.04.25.
 */
public class Api extends ResourceConfig {
	public Api() {
		register(GMAF_Facade_RESTImpl.class);
//		register(JacksonFeature.class);
		packages("org.glassfish.jersey.jackson");

		register(OpenApiResource.class);
		register(SwaggerSerializers.class);
		OpenAPI openAPI = new OpenAPI()
				.info(new Info()
						.title("GMAF API")
						.version("1.0.0")
						.description("API-Documentation")).servers(List.of(
				new Server().url("/gmaf/gmafApi")));

		SwaggerConfiguration config = new SwaggerConfiguration()
				.openAPI(openAPI)
				.resourceClasses(Set.of("de.swa.gmaf.api.GMAF_Facade_RESTImpl"))
				.prettyPrint(true);

		try {
			new JaxrsOpenApiContextBuilder<>()
					.openApiConfiguration(config)
					.buildContext(true);
		} catch (OpenApiConfigurationException e) {
			System.out.println("Error in Config");
			throw new RuntimeException(e);
		}
		System.out.println("Swagger-Configuration loaded");

	}
}
