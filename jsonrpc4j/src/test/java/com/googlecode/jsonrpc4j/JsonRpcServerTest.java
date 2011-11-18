package com.googlecode.jsonrpc4j;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class JsonRpcServerTest {

    private static final String JSONRPC_NOTIFICATION_JSON_FILE = "jsonRpcServerNotificationTest.json";
    
    private ObjectMapper mapper;
    private Service service;
    private ByteArrayOutputStream baos;

    @Before
    public void setup() {
        mapper = new ObjectMapper();
        baos = new ByteArrayOutputStream();
        service = new Service();
    }
    
    @Test
    public void receiveJsonRpcNotification() throws Exception {
        JsonRpcServer jsonRpcServer = new JsonRpcServer(mapper, service);
        jsonRpcServer.handle(new ClassPathResource(JSONRPC_NOTIFICATION_JSON_FILE).getInputStream(), baos);
        
        assertEquals(0, baos.size());
    }
    
    private class Service {
    	@SuppressWarnings("unused")
		public void notificationMethod() {
    	}
    }
    
}
