package de.swa.gmaf;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import de.swa.ui.Configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceTest {
    private static final String TEST_SERVER = "localhost";
    private static final int TEST_SOAP_PORT = 8082;
    private static final int TEST_REST_PORT = 8083;
    private static final String TEST_CONTEXT = "gmaf";
    
    @Mock
    private Configuration mockConfig;
    
    private AutoCloseable closeable;
    private Service service;
    
    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        
        // Configure mock
        when(mockConfig.getServerName()).thenReturn(TEST_SERVER);
        when(mockConfig.getServerPort()).thenReturn(TEST_SOAP_PORT);
        when(mockConfig.getRestServicePort()).thenReturn(TEST_REST_PORT);
        when(mockConfig.getContext()).thenReturn(TEST_CONTEXT);
        when(mockConfig.getCollectionName()).thenReturn("test_collection");
        when(mockConfig.isAutoProcess()).thenReturn(false);
        
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
        TimeUnit.MILLISECONDS.sleep(100);
    }
    
    @Test
    void testServiceStart() throws Exception {
        // Start the service
        service.start();
        
        // Wait for service to start
        TimeUnit.MILLISECONDS.sleep(100);
        
        // Test SOAP endpoint
        String soapUrl = String.format("http://%s:%d/%s/gmafApi/test", TEST_SERVER, TEST_SOAP_PORT, TEST_CONTEXT);
        HttpURLConnection soapConn = (HttpURLConnection) new URL(soapUrl).openConnection();
        soapConn.setRequestMethod("GET");
        assertEquals(200, soapConn.getResponseCode(), "SOAP endpoint should be accessible");
        
        // Test REST endpoint
        String restUrl = String.format("http://%s:%d/%s/gmafApi/gmaf/test", TEST_SERVER, TEST_REST_PORT, TEST_CONTEXT);
        HttpURLConnection restConn = (HttpURLConnection) new URL(restUrl).openConnection();
        restConn.setRequestMethod("GET");
        assertEquals(200, restConn.getResponseCode(), "REST endpoint should be accessible");
    }
    
    @Test
    void testCORSHeaders() throws Exception {
        // Start the service
        service.start();
        
        // Wait for service to start
        TimeUnit.MILLISECONDS.sleep(100);
        
        // Test CORS headers on REST endpoint
        String restUrl = String.format("http://%s:%d/%s/gmafApi/test", TEST_SERVER, TEST_REST_PORT, TEST_CONTEXT);
        HttpURLConnection conn = (HttpURLConnection) new URL(restUrl).openConnection();
        conn.setRequestMethod("OPTIONS");
        
        assertEquals("*", conn.getHeaderField("Access-Control-Allow-Origin"), 
                "Should allow all origins");
        assertTrue(conn.getHeaderField("Access-Control-Allow-Headers")
                .contains("CSRF-Token"), "Should allow CSRF token header");
        assertEquals("true", conn.getHeaderField("Access-Control-Allow-Credentials"), 
                "Should allow credentials");
        assertTrue(conn.getHeaderField("Access-Control-Allow-Methods")
                .contains("GET"), "Should allow GET method");
    }

}
