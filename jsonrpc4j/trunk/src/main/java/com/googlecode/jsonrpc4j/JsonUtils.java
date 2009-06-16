package com.googlecode.jsonrpc4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public class JsonUtils {
    
    public static final JsonFactory FACTORY = new JsonFactory();
    static {
        FACTORY.setCodec(new ObjectMapper());
    }
    
    /**
     * Validates a JSONObject for JSON-RPC 2.0 compliance.
     * @param node the object
     * @return the object if valid
     * @throws JSONException if the object isn't valid
     */
    public static ObjectNode validateRpcRequest(JsonNode node)
        throws JsonParseException {
        
        if (!(node instanceof ObjectNode)) {
            throw new JsonParseException(
                "Source is not an ObjectNode");
        } 
        
        // cast
        ObjectNode obj = ObjectNode.class.cast(node);
        JsonNode versionNode = obj.get("jsonrpc");
        JsonNode methodNode = obj.get("method");
        
        // verify version node
        if (versionNode==null 
            || !versionNode.isTextual() 
            || !versionNode.getTextValue().equals("2.0")) {
            throw new JsonParseException(
                "\"jsonrpc\" attribute not \"2.0\" or not found");
            
        // verify method node
        } else if (methodNode==null
            || !methodNode.isTextual()
            || methodNode.getTextValue().trim().equals("")) {
            throw new JsonParseException(
                "\"method\" attribute empty or not found");
            
        }
        
        return obj;
    }
    
    /**
     * Converts a node to the given type.
     * @param <T> the type
     * @param node the node
     * @param valueType the type
     * @return the value
     * @throws IOException
     */
    public static <T> T convert(JsonNode node, Class<T> valueType) 
        throws IOException {
        
        // TODO: find a better way of going from JsonNode to Class<T>
        
        // convert to json
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator generator = FACTORY.createJsonGenerator(
            out, JsonEncoding.UTF8);
        generator.writeTree(node);
        generator.flush();
        
        // convert to type
        JsonParser parser = FACTORY.createJsonParser(
            new ByteArrayInputStream(out.toByteArray()));
        return parser.readValueAs(valueType);
    }

}
