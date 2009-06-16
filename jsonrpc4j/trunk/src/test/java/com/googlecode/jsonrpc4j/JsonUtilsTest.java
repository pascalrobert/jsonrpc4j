package com.googlecode.jsonrpc4j;

import static org.junit.Assert.*;

import org.codehaus.jackson.JsonNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonUtilsTest {

    @Before
    public void setUp() 
        throws Exception {
    }

    @After
    public void tearDown() 
        throws Exception {
    }

    @Test
    public void testValidateRpcRequest() {
        fail("Not yet implemented");
    }

    @Test
    public void testConvert() 
        throws Exception {
        JsonNode node = JsonUtils.FACTORY.createJsonParser("1").readValueAsTree();
        assertNotNull(node);
        assertEquals(1, (int)JsonUtils.convert(node, int.class));
        assertEquals(new Integer(1), JsonUtils.convert(node, Integer.class));
        assertEquals(1f, (float)JsonUtils.convert(node, float.class), 0.0f);
        assertEquals(new Float(1), JsonUtils.convert(node, Float.class), 0.0f);
        
        node = JsonUtils.FACTORY.createJsonParser("{\"name\": \"Brian\", \"child\": {\"name\": \"Brian\"}}").readValueAsTree();
        assertNotNull(node);
        TestBean result = JsonUtils.convert(node, TestBean.class);
        assertNotNull(result);
        TestService ts = JsonUtils.convert(node, TestService.class);
        assertNotNull(ts);
    }

}
