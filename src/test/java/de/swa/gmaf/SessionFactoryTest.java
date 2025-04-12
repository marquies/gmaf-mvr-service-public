package de.swa.gmaf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.swa.gmaf.api.GMAF_Facade;
import de.swa.gmaf.api.GMAF_Facade_SOAPImpl;

import static org.junit.jupiter.api.Assertions.*;

class SessionFactoryTest {
    
    @BeforeEach
    void setUp() {
        // Reset static fields before each test
        SessionFactory.api = null;
        SessionFactory.sessionId = "";
    }
    
    @Test
    void testApiInitialization() {
        assertNull(SessionFactory.api, "API should initially be null");
        
        GMAF_Facade api = new GMAF_Facade_SOAPImpl();
        SessionFactory.api = api;
        
        assertSame(api, SessionFactory.api, "API should be set correctly");
    }
    
    @Test
    void testSessionIdManagement() {
        assertEquals("", SessionFactory.sessionId, "Session ID should initially be empty");
        
        String testSessionId = "test-session-123";
        SessionFactory.sessionId = testSessionId;
        
        assertEquals(testSessionId, SessionFactory.sessionId, "Session ID should be set correctly");
    }
    
    @Test
    void testConcurrentAccess() throws InterruptedException {
        // Test concurrent access to static fields
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final String sessionId = "session-" + i;
            threads[i] = new Thread(() -> {
                SessionFactory.sessionId = sessionId;
                try {
                    Thread.sleep(10); // Simulate some work
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assertNotNull(SessionFactory.sessionId, "Session ID should never be null");
            });
        }
        
        // Start all threads
        for (Thread t : threads) {
            t.start();
        }
        
        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }
    }
}
