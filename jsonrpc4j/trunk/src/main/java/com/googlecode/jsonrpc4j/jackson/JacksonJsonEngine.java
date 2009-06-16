package com.googlecode.jsonrpc4j.jackson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.codehaus.jackson.map.ser.StdSerializerProvider;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.googlecode.jsonrpc4j.JsonEngine;
import com.googlecode.jsonrpc4j.JsonException;

public class JacksonJsonEngine 
	implements JsonEngine {

	private JsonFactory jsonFactory;
	private AliasDeserializerFactory aliasDeserializationFactory;
	
	/**
	 * 
	 * @param serializer
	 * @param deserializer
	 * @param serializerConfig
	 * @param deserializerConfig
	 */
	public JacksonJsonEngine() {
		
		// create the factory
		jsonFactory = new JsonFactory();
		aliasDeserializationFactory = new AliasDeserializerFactory();
		
		// create the object mapper
		ObjectMapper mapper = new ObjectMapper(
			jsonFactory, new StdSerializerProvider(), 
			new StdDeserializerProvider(aliasDeserializationFactory));
		jsonFactory.setCodec(mapper);
		
	}

	/**
     * TODO: find a better way of going from JsonNode to Object.
	 * {@inheritDoc}
	 */
	public <T> T jsonToObject(Object json, Class<T> valueType) 
		throws JsonException {
		
		// make sure it's what we expect
        if (!(json instanceof JsonNode)) {
            throw new JsonException(
                "Source is not a JsonNode");
        }
        
        try {
        	// convert to json string
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        JsonGenerator generator = jsonFactory.createJsonGenerator(
	            out, JsonEncoding.UTF8);
	        generator.writeTree(JsonNode.class.cast(json));
	        generator.flush();
	        
	        // convert to type
	        JsonParser parser = jsonFactory.createJsonParser(
	            new ByteArrayInputStream(out.toByteArray()));
	        return parser.readValueAs(valueType);
	        
        } catch(Exception e) {
        	throw new JsonException(e);
        }
        
	}

	/**
     * TODO: find a better way of going from Object to JsonNode.
	 * {@inheritDoc}
	 */
	public <T> Object objectToJson(T obj) 
		throws JsonException {
		
        try {
        	// convert to json string
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        JsonGenerator generator = jsonFactory.createJsonGenerator(
	            out, JsonEncoding.UTF8);
	        generator.writeObject(obj);
	        generator.flush();
	        
	        // convert to JsonNode
	        JsonParser parser = jsonFactory.createJsonParser(
	            new ByteArrayInputStream(out.toByteArray()));
	        return parser.readValueAsTree();
	        
        } catch(Exception e) {
        	throw new JsonException(e);
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public Object validateRpcRequest(Object json) 
		throws JsonException {
		
		// make sure it's what we expect
		if (!(json instanceof ObjectNode)) {
            throw new JsonException(
                "Source is not an ObjectNode");
        } 
        
        // cast
        ObjectNode obj = ObjectNode.class.cast(json);
        JsonNode versionNode = obj.get("jsonrpc");
        JsonNode methodNode = obj.get("method");
        
        // verify version node
        if (versionNode==null 
            || !versionNode.isTextual() 
            || !versionNode.getTextValue().equals("2.0")) {
            throw new JsonException(
                "\"jsonrpc\" attribute not \"2.0\" or not found");
            
        // verify method node
        } else if (methodNode==null
            || !methodNode.isTextual()
            || methodNode.getTextValue().trim().equals("")) {
            throw new JsonException(
                "\"method\" attribute empty or not found");
            
        }
        
        return obj;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object validateRpcBatchRequest(Object json) 
		throws JsonException {
		if (!(json instanceof ArrayNode)) {
            throw new JsonException(
                "Source is not an ArrayNode");
        }
		ArrayNode array = ArrayNode.class.cast(json);
		for (int i=0; i<array.size(); i++) {
			validateRpcRequest(array.get(i));
		}
        return array;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRpcBatchRequest(Object json) 
		throws JsonException {
		return (json instanceof ArrayNode);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isNotification(Object json) 
		throws JsonException {
		return (!(json instanceof ObjectNode))
			? false : ((ObjectNode)json).get("id").isNull() 
				|| ((ObjectNode)json).get("id").isMissingNode();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Iterator<Object> getRpcBatchIterator(Object json) 
		throws JsonException {
		if (!(json instanceof ArrayNode)) {
            throw new JsonException(
                "Source is not an ArrayNode");
        }
        return (Iterator)ArrayNode.class.cast(json).iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object readJson(InputStream in) 
		throws JsonException {
		try {
			JsonParser parser = jsonFactory.createJsonParser(in);
			return parser.readValueAsTree();
		} catch(Exception e) {
			throw new JsonException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void writeJson(Object json, OutputStream out) 
		throws JsonException {
		
		// make sure it's what we expect
        if (!(json instanceof JsonNode)) {
            throw new JsonException(
                "Source is not a JsonNode");
        } else if (json instanceof ObjectNode) {
        	validateRpcRequest(json);
        }
        
        try {
        	// convert to json string
	        JsonGenerator generator = jsonFactory.createJsonGenerator(
	        	out, JsonEncoding.UTF8);
	        generator.writeTree(JsonNode.class.cast(json));
	        generator.flush();
	        
        } catch(Exception e) {
        	throw new JsonException(e);
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public void addTypeAlias(Class<?> fromType, Class<?> toType) {
		aliasDeserializationFactory.addAlias(fromType, toType);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIdFromRpcRequest(Object json) 
		throws JsonException {
		
		// make sure it's what we expect
        if (!(json instanceof ObjectNode)) {
            throw new JsonException(
                "Source is not a ObjectNode");
        }
        
        ObjectNode node = (ObjectNode)json;
        return node.get("id").getValueAsText();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Object> getJsonParametersFromRpcRequest(Object json) 
		throws JsonException {
		
		// make sure it's what we expect
        if (!(json instanceof ObjectNode)) {
            throw new JsonException(
                "Source is not a ObjectNode");
        }
        
        List<Object> ret = new ArrayList<Object>();
        ObjectNode node = (ObjectNode)json;
        if (node.get("params").isArray()) {
        	ArrayNode array = (ArrayNode)node.get("params");
        	for (int i=0; i<array.size(); i++) {
        		ret.add(array.get(i));
        	}
        	return ret;
        } else {
        	ret.add(node.get("params"));
        }
        return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMethodNameFromRpcRequest(Object json) 
		throws JsonException {
		
		// make sure it's what we expect
        if (!(json instanceof ObjectNode)) {
            throw new JsonException(
                "Source is not a ObjectNode");
        }
        
        ObjectNode node = (ObjectNode)json;
        return node.get("method").getValueAsText();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRpcRequestParametersIndexed(Object json)
		throws JsonException {
		
		// make sure it's what we expect
        if (!(json instanceof ObjectNode)) {
            throw new JsonException(
                "Source is not a ObjectNode");
        }
        
        ObjectNode node = (ObjectNode)json;
        return node.get("params").isArray();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getRpcRequestParameter(Object json, int index)
		throws JsonException {
		
		// make sure it's what we expect
        if (!isRpcRequestParametersIndexed(json)) {
            throw new JsonException(
                "JSON-RPC request params are not indexed");
        }
        
        return ((ArrayNode)((ObjectNode)json).get("params")).get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getRpcRequestParameter(Object json, String name)
		throws JsonException {
		
		// make sure it's what we expect
        if (isRpcRequestParametersIndexed(json)) {
            throw new JsonException(
            	"JSON-RPC request params are not named");
        }
        
        return ((ObjectNode)((ObjectNode)json).get("params")).get(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getRpcRequestParameterCount(Object json) 
		throws JsonException {
		
		// make sure it's what we expect
        if (!(json instanceof ObjectNode)) {
            throw new JsonException(
                "Source is not a ObjectNode");
        }
        
        ObjectNode node = (ObjectNode)json;
        return node.get("params").size();
	}

}
