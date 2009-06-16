package com.googlecode.jsonrpc4j.spring;

import static org.junit.Assert.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.googlecode.jsonrpc4j.JavaBean;
import com.googlecode.jsonrpc4j.TestService;
import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;


@RunWith(JMock.class)
public class JsonServiceExporterTest {

    private Mockery mockCtx = new JUnit4Mockery();
    private JsonServiceExporter exporter = null;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private JSONObject rpcRequest;
    
    @Before
    public void setUp() 
        throws Exception {
        exporter = new JsonServiceExporter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        rpcRequest = new JSONObject();
    }

    @After
    public void tearDown() 
        throws Exception {
        exporter = null;
        request = null;
        response = null;
        rpcRequest = null;
    }

    @Test
    public void testHandleRequest_execVoid() 
        throws Exception {
        final TestService service = mockCtx.mock(TestService.class);
        mockCtx.checking(new Expectations() {{ 
            one(service).execVoid();
        }});
        
        exporter.setStrict(false);
        exporter.setService(service);
        exporter.setServiceInterface(TestService.class);
        exporter.afterPropertiesSet();
        
        String id = "The Id Here "+System.currentTimeMillis();
        
        rpcRequest.accumulate("method", "execVoid");
        rpcRequest.accumulate("id", id);
        rpcRequest.accumulate("params", new Object[0]);
        
        request.setContent(rpcRequest.toString().getBytes());
        request.setMethod("POST");
        request.setContentType(JsonServiceExporter.JSONRPC_REQUEST_CONTENT_TYPES[0]);
        
        exporter.handleRequest(request, response);
        
        JSONObject rpcResponse = JSONObject.fromObject(response.getContentAsString());
        assertNotNull(rpcResponse);
        assertEquals(id, rpcResponse.getString("id"));
        assertNotNull(rpcResponse.getString("error"));
        assertNotNull(rpcResponse.getString("result"));
    }

    @Test
    public void testHandleRequest_execInt() 
        throws Exception {
        final TestService service = mockCtx.mock(TestService.class);
        mockCtx.checking(new Expectations() {{ 
            one(service).execInt();
            will(returnValue(12));
        }});
        
        exporter.setStrict(false);
        exporter.setService(service);
        exporter.setServiceInterface(TestService.class);
        exporter.afterPropertiesSet();
        
        String id = "The Id Here "+System.currentTimeMillis();
        
        rpcRequest.accumulate("method", "execInt");
        rpcRequest.accumulate("id", id);
        rpcRequest.accumulate("params", new Object[0]);
        
        request.setContent(rpcRequest.toString().getBytes());
        request.setMethod("POST");
        request.setContentType(JsonServiceExporter.JSONRPC_REQUEST_CONTENT_TYPES[0]);
        
        exporter.handleRequest(request, response);
        
        JSONObject rpcResponse = JSONObject.fromObject(response.getContentAsString());
        assertNotNull(rpcResponse);
        assertEquals(id, rpcResponse.getString("id"));
        assertNotNull(rpcResponse.getString("error"));
        assertEquals("12", rpcResponse.getString("result"));
    }

    @Test
    public void testHandleRequest_execLong() 
        throws Exception {
        final TestService service = mockCtx.mock(TestService.class);
        mockCtx.checking(new Expectations() {{ 
            one(service).execLong();
            will(returnValue(new Long(12312)));
        }});
        
        exporter.setStrict(false);
        exporter.setService(service);
        exporter.setServiceInterface(TestService.class);
        exporter.afterPropertiesSet();
        
        String id = "The Id Here "+System.currentTimeMillis();
        
        rpcRequest.accumulate("method", "execLong");
        rpcRequest.accumulate("id", id);
        rpcRequest.accumulate("params", new Object[0]);
        
        request.setContent(rpcRequest.toString().getBytes());
        request.setMethod("POST");
        request.setContentType(JsonServiceExporter.JSONRPC_REQUEST_CONTENT_TYPES[0]);
        
        exporter.handleRequest(request, response);
        
        JSONObject rpcResponse = JSONObject.fromObject(response.getContentAsString());
        assertNotNull(rpcResponse);
        assertEquals(id, rpcResponse.getString("id"));
        assertNotNull(rpcResponse.getString("error"));
        assertEquals("12312", rpcResponse.getString("result"));
    }

    @Test
    public void testHandleRequest_execString() 
        throws Exception {
        final TestService service = mockCtx.mock(TestService.class);
        mockCtx.checking(new Expectations() {{ 
            one(service).execString(12, 13L);
            will(returnValue("sack of balls"));
        }});
        
        exporter.setStrict(false);
        exporter.setService(service);
        exporter.setServiceInterface(TestService.class);
        exporter.afterPropertiesSet();
        
        String id = "The Id Here "+System.currentTimeMillis();
        
        rpcRequest.accumulate("method", "execString");
        rpcRequest.accumulate("id", id);
        rpcRequest.accumulate("params", new Object[] {12, 13L });
        
        request.setContent(rpcRequest.toString().getBytes());
        request.setMethod("POST");
        request.setContentType(JsonServiceExporter.JSONRPC_REQUEST_CONTENT_TYPES[0]);
        
        exporter.handleRequest(request, response);
        
        JSONObject rpcResponse = JSONObject.fromObject(response.getContentAsString());
        assertNotNull(rpcResponse);
        assertEquals(id, rpcResponse.getString("id"));
        assertNotNull(rpcResponse.getString("error"));
        assertEquals("sack of balls", rpcResponse.getString("result"));
    }

    @Test
    public void testHandleRequest_execBoolean() 
        throws Exception {
        final TestService service = mockCtx.mock(TestService.class);
        mockCtx.checking(new Expectations() {{ 
            one(service).execBoolean();
            will(returnValue(new Boolean(true)));
        }});
        
        exporter.setStrict(false);
        exporter.setService(service);
        exporter.setServiceInterface(TestService.class);
        exporter.afterPropertiesSet();
        
        String id = "The Id Here "+System.currentTimeMillis();
        
        rpcRequest.accumulate("method", "execBoolean");
        rpcRequest.accumulate("id", id);
        rpcRequest.accumulate("params", new Object[] {});
        
        request.setContent(rpcRequest.toString().getBytes());
        request.setMethod("POST");
        request.setContentType(JsonServiceExporter.JSONRPC_REQUEST_CONTENT_TYPES[0]);
        
        exporter.handleRequest(request, response);
        
        JSONObject rpcResponse = JSONObject.fromObject(response.getContentAsString());
        assertNotNull(rpcResponse);
        assertEquals(id, rpcResponse.getString("id"));
        assertNotNull(rpcResponse.getString("error"));
        assertTrue(rpcResponse.getBoolean("result"));
    }

    @Test
    public void testHandleRequest_execJavaBean() 
        throws Exception {
        final TestService service = mockCtx.mock(TestService.class);
        final JavaBean javaBean = new JavaBean();
        javaBean.setAge(234234);
        javaBean.setId(435L);
        javaBean.setName("The Name");
        mockCtx.checking(new Expectations() {{ 
            one(service).execJavaBean();
            will(returnValue(javaBean));
        }});
        
        exporter.setStrict(false);
        exporter.setService(service);
        exporter.setServiceInterface(TestService.class);
        exporter.afterPropertiesSet();
        
        String id = "The Id Here "+System.currentTimeMillis();
        
        rpcRequest.accumulate("method", "execJavaBean");
        rpcRequest.accumulate("id", id);
        rpcRequest.accumulate("params", new Object[] {});
        
        request.setContent(rpcRequest.toString().getBytes());
        request.setMethod("POST");
        request.setContentType(JsonServiceExporter.JSONRPC_REQUEST_CONTENT_TYPES[0]);
        
        exporter.handleRequest(request, response);
        
        JSONObject rpcResponse = JSONObject.fromObject(response.getContentAsString());
        assertNotNull(rpcResponse);
        assertEquals(id, rpcResponse.getString("id"));
        assertNotNull(rpcResponse.getString("error"));
        assertTrue(rpcResponse.get("result") instanceof JSONObject);
        JSONObject result = (JSONObject)rpcResponse.get("result");
        assertEquals(javaBean.getName(), result.getString("name"));
        assertEquals(javaBean.getId().longValue(), result.getLong("id"));
        assertEquals(javaBean.getAge(), result.getInt("age"));
    }

    @Test
    public void testHandleRequest_execJavaBean2() 
        throws Exception {
        final TestService service = mockCtx.mock(TestService.class);
        
        final JavaBean javaBean = new JavaBean();
        javaBean.setAge(234234);
        javaBean.setId(435L);
        javaBean.setName("The Name");
        
        mockCtx.checking(new Expectations() {{ 
            one(service).execJavaBean2(javaBean, 12);
        }});
        
        exporter.setStrict(false);
        exporter.setService(service);
        exporter.setServiceInterface(TestService.class);
        exporter.afterPropertiesSet();
        
        String id = "The Id Here "+System.currentTimeMillis();
        
        rpcRequest.accumulate("method", "execJavaBean2");
        rpcRequest.accumulate("id", id);
        rpcRequest.accumulate("params", new JSONObject()
        	.accumulate("bean",
	        	new JSONObject()
	        		.accumulate("age", javaBean.getAge())
	        		.accumulate("id", javaBean.getId())
	        		.accumulate("name", javaBean.getName()))
	        .accumulate("age", 12)
        );
        
        request.setContent(rpcRequest.toString().getBytes());
        request.setMethod("POST");
        request.setContentType(JsonServiceExporter.JSONRPC_REQUEST_CONTENT_TYPES[0]);
        
        exporter.handleRequest(request, response);
        
        JSONObject rpcResponse = JSONObject.fromObject(response.getContentAsString());
        assertNotNull(rpcResponse);
        assertEquals(id, rpcResponse.getString("id"));
        assertNotNull(rpcResponse.getString("error"));
    }

    @Test
    public void testHandleRequest_execJavaBean3() 
        throws Exception {
        final TestService service = mockCtx.mock(TestService.class);
        
        final JavaBean javaBean = new JavaBean();
        javaBean.setAge(234234);
        javaBean.setId(435L);
        javaBean.setName("The Name");
        
        mockCtx.checking(new Expectations() {{ 
            one(service).execJavaBean(new int[] { 3, 2, 1});
            will(returnValue(javaBean));
        }});
        
        exporter.setStrict(false);
        exporter.setService(service);
        exporter.setServiceInterface(TestService.class);
        exporter.afterPropertiesSet();
        
        String id = "The Id Here "+System.currentTimeMillis();
        
        rpcRequest.accumulate("method", "execJavaBean");
        rpcRequest.accumulate("id", id);
        rpcRequest.accumulate("params", new JSONArray().element(
        	new JSONArray().element(3).element(2).element(1)
        ));
        
        request.setContent(rpcRequest.toString().getBytes());
        request.setMethod("POST");
        request.setContentType(JsonServiceExporter.JSONRPC_REQUEST_CONTENT_TYPES[0]);
        
        exporter.handleRequest(request, response);
        
        JSONObject rpcResponse = JSONObject.fromObject(response.getContentAsString());
        assertNotNull(rpcResponse);
        assertEquals(id, rpcResponse.getString("id"));
        assertNotNull(rpcResponse.getString("error"));
        assertTrue(rpcResponse.get("result") instanceof JSONObject);
        JSONObject result = (JSONObject)rpcResponse.get("result");
        assertEquals(javaBean.getName(), result.getString("name"));
        assertEquals(javaBean.getId().longValue(), result.getLong("id"));
        assertEquals(javaBean.getAge(), result.getInt("age"));
    }

}
