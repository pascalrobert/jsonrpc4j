package com.googlecode.jsonrpc4j;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Reads JSONObject
 * @author brian
 *
 */
public class JsonRpcReader {
    
    private JsonParser parser = null;
    
    public JsonRpcReader(Reader reader) 
        throws JsonParseException {
        try {
            parser = JsonUtils.FACTORY.createJsonParser(reader);
        } catch(Throwable t) {
            throw new JsonParseException(t);
        }
    }
    
    public JsonRpcReader(InputStream input)
        throws JsonParseException {
        try {
            parser = JsonUtils.FACTORY.createJsonParser(input);
        } catch(Throwable t) {
            throw new JsonParseException(t);
        }
    }
    
    public JsonRpcReader(String input)
        throws JsonParseException {
        try {
            parser = JsonUtils.FACTORY.createJsonParser(input);
        } catch(Throwable t) {
            throw new JsonParseException(t);
        }
    }
    
    /**
     * Reads the next request(s).  In the case
     * of a single (non-batch) request the list
     * returned is only 1 element long.
     * @return the request(s)
     * @throws JSONException
     */
    public List<ObjectNode> readNext() 
        throws JsonParseException {
        
        // get the next value
        JsonNode node = null;
        try {
            node = parser.readValueAsTree();
        } catch(Throwable t) {
            throw new JsonParseException(t);
        }
        
        // prepare return list
        List<ObjectNode> ret = new ArrayList<ObjectNode>();
        
        // single request
        if (node instanceof ArrayNode) {
            ArrayNode jsonArray = ArrayNode.class.cast(node);
            for (int i=0; i<jsonArray.size(); i++) {
                ret.add(JsonUtils.validateRpcRequest(jsonArray.get(i)));
            }
            
        // something else
        } else {
            ret.add(JsonUtils.validateRpcRequest(node));
        }
        
        // return the stuff
        return ret;
    }
    
}
