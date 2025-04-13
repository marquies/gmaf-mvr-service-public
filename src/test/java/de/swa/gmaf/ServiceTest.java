package de.swa.gmaf;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import de.swa.ui.Configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceTest {
    private static final String TEST_SERVER = "localhost";
    private static final int TEST_SOAP_PORT = 8481;
    private static final int TEST_REST_PORT = 8482;
    private static final String TEST_CONTEXT = "gmaf";
    
    @Mock
    private Configuration mockConfig;
    
    private AutoCloseable closeable;
    private Service service;
    
    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        
        // Configure mock
//        when(mockConfig.getServerName()).thenReturn(TEST_SERVER);
//        when(mockConfig.getServerPort()).thenReturn(TEST_SOAP_PORT);
//        when(mockConfig.getRestServicePort()).thenReturn(TEST_REST_PORT);
//        when(mockConfig.getContext()).thenReturn(TEST_CONTEXT);
//        when(mockConfig.getCollectionName()).thenReturn("test_collection");
//        when(mockConfig.isAutoProcess()).thenReturn(false);
        
        // Replace singleton instance with mock
        Configuration.setInstance(mockConfig);
        
        // Create service instance
        service = new Service();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (service != null) {
            service.stop();
        }
        closeable.close();
        //Configuration.reset();
        
        // Wait for ports to be released
        java.util.concurrent.TimeUnit.MILLISECONDS.sleep(100);
    }
    
    @Test
    void testServiceStart() throws Exception {
        // Start the service
        service.start();
        
        // Wait for service to start
        java.util.concurrent.TimeUnit.MILLISECONDS.sleep(100);
        
        // Test SOAP endpoint
        String soapUrl = String.format("http://%s:%d/%s/gmafApi/test", TEST_SERVER, TEST_SOAP_PORT, TEST_CONTEXT);
        java.net.HttpURLConnection soapConn = (java.net.HttpURLConnection) new java.net.URL(soapUrl).openConnection();
        soapConn.setRequestMethod("GET");
        assertEquals(200, soapConn.getResponseCode(), "SOAP endpoint should be accessible");
        
        // Test REST endpoint
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s:%d/%s/gmafApi/gmaf/test", TEST_SERVER, TEST_REST_PORT, TEST_CONTEXT)))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }
    
    @Test
    void testCORSHeaders() throws Exception {
        // Start the service
        service.start();
        
        // Wait for service to start
        java.util.concurrent.TimeUnit.MILLISECONDS.sleep(100);
        
        // Test CORS headers on REST endpoint
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s:%d/%s/gmafApi/gmaf/test", TEST_SERVER, TEST_REST_PORT, TEST_CONTEXT)))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        String allowOrigin = response.headers().firstValue("Access-Control-Allow-Origin").orElse(null);
        String allowMethods = response.headers().firstValue("Access-Control-Allow-Methods").orElse(null);
        String allowHeaders = response.headers().firstValue("Access-Control-Allow-Headers").orElse(null);

        assertNotNull(allowOrigin, "Access-Control-Allow-Origin header should be present");
        assertNotNull(allowMethods, "Access-Control-Allow-Methods header should be present");
        assertNotNull(allowHeaders, "Access-Control-Allow-Headers header should be present");

        assertEquals("*", allowOrigin);
        assertTrue(allowMethods.contains("GET"));
        assertTrue(allowMethods.contains("POST"));
//        assertTrue(allowHeaders.contains("CSRF-Token"));
    }
}
