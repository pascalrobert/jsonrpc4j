package com.googlecode.jsonrpc4j;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonRpcReaderTest {

    private JsonRpcReader reader = null;
    
    private JsonRpcReader create(Reader reader) 
        throws JsonParseException {
        return new JsonRpcReader(reader);
    }
    
    private JsonRpcReader create(InputStream input) 
        throws JsonParseException {
        return new JsonRpcReader(input);
    }
    
    private JsonRpcReader create(String string) 
        throws JsonParseException {
        return new JsonRpcReader(string);
    }
    
    @Before
    public void setUp() 
        throws Exception {
        
    }

    @After
    public void tearDown() 
        throws Exception {
        reader = null;
    }

    @Test
    public void testReadNext_String_1Request() 
        throws Exception {
        
        StringBuilder buff = new StringBuilder()
            .append("{")
            .append("\"jsonrpc\":\"2.0\",")
            .append("\"method\":\"testMethod\",")
            .append("\"params\":1,")
            .append("\"id\":\"the-id\"")
            .append("}");
        
        reader = create(buff.toString());
        
        List<ObjectNode> nodesList = reader.readNext();
        assertNotNull(nodesList);
        assertEquals(1, nodesList.size());
        ObjectNode node = (ObjectNode)nodesList.get(0);
        assertNotNull(node);
        assertEquals("testMethod", node.get("method").getTextValue());
        assertEquals(1d, node.get("params").getDoubleValue(), 0.0);
        assertEquals("the-id", node.get("id").getTextValue());
        assertEquals("2.0", node.get("jsonrpc").getTextValue());
        
    }

    @Test
    public void testReadNext_InputStream_2Request() 
        throws Exception {
        
        StringBuilder buff = new StringBuilder()
            .append("[{")
            .append("\"jsonrpc\":\"2.0\",")
            .append("\"method\":\"testMethod\",")
            .append("\"params\":1,")
            .append("\"id\":\"the-id 1\"")
            .append("},{")
            .append("\"jsonrpc\":\"2.0\",")
            .append("\"method\":\"testMethod\",")
            .append("\"params\":1,")
            .append("\"id\":\"the-id 2\"")
            .append("}]");
        
        reader = create(IOUtils.toInputStream(buff.toString()));
        
        List<ObjectNode> nodesList = reader.readNext();
        assertNotNull(nodesList);
        assertEquals(2, nodesList.size());
        ObjectNode node = (ObjectNode)nodesList.get(0);
        assertNotNull(node);
        assertEquals("testMethod", node.get("method").getTextValue());
        assertEquals(1d, node.get("params").getDoubleValue(), 0.0);
        assertEquals("the-id 1", node.get("id").getTextValue());
        assertEquals("2.0", node.get("jsonrpc").getTextValue());
        node = (ObjectNode)nodesList.get(1);
        assertNotNull(node);
        assertEquals("testMethod", node.get("method").getTextValue());
        assertEquals(1d, node.get("params").getDoubleValue(), 0.0);
        assertEquals("the-id 2", node.get("id").getTextValue());
        assertEquals("2.0", node.get("jsonrpc").getTextValue());
        
    }

    @Test
    public void testReadNext_String_1Request_ArrayParams() 
        throws Exception {
        
        StringBuilder buff = new StringBuilder()
            .append("{")
            .append("\"jsonrpc\":\"2.0\",")
            .append("\"method\":\"testMethod\",")
            .append("\"params\":[\"balls\", 12, 34.9, {\"anus\" : 1}],")
            .append("\"id\":\"the-id\"")
            .append("}");
        
        reader = create(buff.toString());
        
        List<ObjectNode> nodesList = reader.readNext();
        assertNotNull(nodesList);
        assertEquals(1, nodesList.size());
        ObjectNode node = (ObjectNode)nodesList.get(0);
        assertNotNull(node);
        assertEquals("testMethod", node.get("method").getTextValue());
        assertEquals("the-id", node.get("id").getTextValue());
        assertEquals("2.0", node.get("jsonrpc").getTextValue());
        assertTrue(node.get("params").isArray());
        ArrayNode arrayNode = (ArrayNode)node.get("params");
        assertEquals(4, arrayNode.size());
        assertEquals("balls", arrayNode.get(0).getTextValue());
        assertEquals(12, arrayNode.get(1).getIntValue());
        assertEquals(34.9d, arrayNode.get(2).getDoubleValue(), 0.0);
        assertTrue(arrayNode.get(3).isObject());
        
    }

    @Test
    public void testReadNext_Reader_1Request_ObjectParams() 
        throws Exception {
        
        StringBuilder buff = new StringBuilder()
            .append("{")
            .append("\"jsonrpc\":\"2.0\",")
            .append("\"method\":\"testMethod\",")
            .append("\"params\":{\"balls\": \"large\", \"x\": 12},")
            .append("\"id\":\"the-id\"")
            .append("}");
        
        reader = create(new InputStreamReader(IOUtils.toInputStream(buff.toString())));
        
        List<ObjectNode> nodesList = reader.readNext();
        assertNotNull(nodesList);
        assertEquals(1, nodesList.size());
        ObjectNode node = (ObjectNode)nodesList.get(0);
        assertNotNull(node);
        assertEquals("testMethod", node.get("method").getTextValue());
        assertEquals("the-id", node.get("id").getTextValue());
        assertEquals("2.0", node.get("jsonrpc").getTextValue());
        assertTrue(node.get("params").isObject());
        ObjectNode objNode = (ObjectNode)node.get("params");
        assertEquals(2, objNode.size());
        assertEquals("large", objNode.get("balls").getTextValue());
        assertEquals(12, objNode.get("x").getIntValue());
        
    }

}
