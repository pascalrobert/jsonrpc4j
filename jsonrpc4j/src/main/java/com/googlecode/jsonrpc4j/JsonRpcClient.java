package com.googlecode.jsonrpc4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Random;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.node.ObjectNode;

/**
 * A JSON-RPC client.
 */
public class JsonRpcClient {

	private static final String JSON_RPC_VERSION = "2.0";

	private ObjectMapper mapper;
	private Random random;
	private RequestListener requestListener;

	/**
	 * Creates a client that uses the given {@link ObjectMapper} to
	 * map to and from JSON and Java objects.
	 * @param mapper the {@link ObjectMapper}
	 */
	public JsonRpcClient(ObjectMapper mapper) {
		this.mapper = mapper;
		this.random = new Random(System.currentTimeMillis());
	}

	/**
	 * Creates a client that uses the default {@link ObjectMapper}
	 * to map to and from JSON and Java objects.
	 */
	public JsonRpcClient() {
		this(new ObjectMapper());
	}

	/**
	 * Sets the {@link RequestListener}.
	 * @param requestListener the {@link RequestListener}
	 */
	public void setRequestListener(RequestListener requestListener) {
		this.requestListener = requestListener;
	}

	/**
	 * Invokes the given method on the remote service
	 * passing the given arguments and reads a response
	 * expecting a return value.  This is a standard
	 * JSON-RPC request.
	 *
	 * @param methodName the method to invoke
	 * @param arguments the arguments to pass to the method
	 * @param returnType the expected return type
	 * @param ops the {@link OutputStream} to write to
	 * @param ips the {@link InputStream} to read from
	 * @return the returned Object
	 * @throws Exception on error
	 */
	public Object invokeAndReadResponse(
		String methodName, Object[] arguments, Type returnType,
		OutputStream ops, InputStream ips)
		throws Exception {
		return invokeAndReadResponse(
			methodName, arguments, returnType, ops, ips, random.nextLong()+"");
	}

	/**
	 * Invokes the given method on the remote service
	 * passing the given arguments and reads a response
	 * expecting a return value.  This is a standard
	 * JSON-RPC request.
	 *
	 * @param methodName the method to invoke
	 * @param arguments the arguments to pass to the method
	 * @param returnType the expected return type
	 * @param ops the {@link OutputStream} to write to
	 * @param ips the {@link InputStream} to read from
	 * @param id id to send with the JSON-RPC request
	 * @return the returned Object
	 * @throws Exception on error
	 */
	public Object invokeAndReadResponse(
		String methodName, Object[] arguments, Type returnType,
		OutputStream ops, InputStream ips, String id)
		throws Exception {

		// invoke it
		invoke(methodName, arguments, ops, id);

		// read it
		return readResponse(returnType, ips);
	}

	/**
	 * Invokes the given method on the remote service passing
	 * the given arguments without reading a return response.
	 * An id is generated.
	 *
	 * @param methodName the method to invoke
	 * @param arguments the arguments to pass to the method
	 * @param ops the {@link OutputStream} to write to
	 * @throws Exception on error
	 */
	public void invoke(
		String methodName, Object[] arguments, OutputStream ops)
		throws Exception {
		invoke(methodName, arguments, ops, random.nextLong()+"");
	}

	/**
	 * Invokes the given method on the remote service passing
	 * the given arguments without reading a return response.
	 *
	 * @param methodName the method to invoke
	 * @param arguments the arguments to pass to the method
	 * @param ops the {@link OutputStream} to write to
	 * @param id the request id
	 * @throws Exception on error
	 */
	public void invoke(
		String methodName, Object[] arguments, OutputStream ops, String id)
		throws Exception {
		writeRequest(methodName, arguments, ops, id);
		ops.flush();
	}

	/**
	 * Invokes the given method on the remote service passing
	 * the given arguments without expecting a return response.
	 *
	 * @param methodName the method to invoke
	 * @param arguments the arguments to pass to the method
	 * @param ops the {@link OutputStream} to write to
	 * @throws Exception on error
	 */
	public void invokeNotification(
		String methodName, Object[] arguments, OutputStream ops)
		throws Exception {
		writeNotification(methodName, arguments, ops);
		ops.flush();
	}

	/**
	 * Reads a JSON-PRC response from the server.  This blocks until
	 * a response is received.
	 *
	 * @param returnType the expected return type
	 * @param ips the {@link InputStream} to read from
	 * @return the object returned by the JSON-RPC response
	 * @throws Exception on error
	 */
	public Object readResponse(Type returnType, InputStream ips)
		throws Exception {

		// read the response
		JsonNode response = mapper.readTree(ips);

		// bail on invalid response
		if (!response.isObject()) {
			throw new Exception("Invalid JSON-RPC response");
		}
		ObjectNode jsonObject = ObjectNode.class.cast(response);

		// show to listener
		if (this.requestListener!=null) {
			this.requestListener.onBeforeResponseProcessed(this, jsonObject);
		}

		// detect errors
		if (jsonObject.has("error")
			&& jsonObject.get("error")!=null
			&& !jsonObject.get("error").isNull()) {
			ObjectNode errorObject = ObjectNode.class.cast(jsonObject.get("error"));
			throw new Exception(
				"JSON-RPC Error "+errorObject.get("code")+": "+errorObject.get("message"));
		}

		// convert it to a return object
		if (jsonObject.has("result")
			&& !jsonObject.get("result").isNull()
			&& jsonObject.get("result")!=null) {
			return mapper.readValue(
				jsonObject.get("result"), TypeFactory.type(returnType));
		}

		// no return type
		return null;
	}

	/**
	 * Writes a JSON-RPC request to the given
	 * {@link OutputStream}.
	 *
	 * @param methodName the method to invoke
	 * @param arguments the method arguments
	 * @param ops the {@link OutputStream} to write to
	 * @param id the request id
	 * @throws IOException on error
	 */
	public void writeRequest(
		String methodName, Object[] arguments, OutputStream ops, String id)
		throws IOException {

		// create the request
		ObjectNode request = mapper.createObjectNode();
		request.put("id", id);
		request.put("jsonrpc", JSON_RPC_VERSION);
		request.put("method", methodName);
		request.put("params", mapper.valueToTree(arguments));

		// show to listener
		if (this.requestListener!=null) {
			this.requestListener.onBeforeRequestSent(this, request);
		}

		// post the json data;
		mapper.writeValue(ops, request);
	}

	/**
	 * Writes a JSON-RPC notification to the given
	 * {@link OutputStream}.
	 *
	 * @param methodName the method to invoke
	 * @param arguments the method arguments
	 * @param ops the {@link OutputStream} to write to
	 * @throws IOException on error
	 */
	public void writeNotification(
		String methodName, Object[] arguments, OutputStream ops)
		throws IOException {

		// create the request
		ObjectNode request = mapper.createObjectNode();
		request.put("jsonrpc", JSON_RPC_VERSION);
		request.put("method", methodName);
		request.put("params", mapper.valueToTree(arguments));

		// show to listener
		if (this.requestListener!=null) {
			this.requestListener.onBeforeRequestSent(this, request);
		}

		// post the json data;
		mapper.writeValue(ops, request);
	}

	/**
	 * Returns the {@link ObjectMapper} that the client
	 * is using for JSON marshalling.
	 * @return the {@link ObjectMapper}
	 */
	public ObjectMapper getObjectMapper() {
		return mapper;
	}

	/**
	 * Provides access to the jackson {@link ObjectNode}s
	 * that represent the JSON-RPC requests and responses.
	 *
	 */
	public interface RequestListener {

		/**
		 * Called before a request is sent to the
		 * server end-point.  Modifications can be
		 * made to the request before it's sent.
		 * @param client the {@link JsonRpcClient}
		 * @param request the request {@link ObjectNode}
		 */
		void onBeforeRequestSent(JsonRpcClient client, ObjectNode request);

		/**
		 * Called after a response has been returned and
		 * successfully parsed but before it has been
		 * processed and turned into java objects.
		 * @param client the {@link JsonRpcClient}
		 * @param response the response {@link ObjectNode}
		 */
		void onBeforeResponseProcessed(JsonRpcClient client, ObjectNode response);
	}

}
