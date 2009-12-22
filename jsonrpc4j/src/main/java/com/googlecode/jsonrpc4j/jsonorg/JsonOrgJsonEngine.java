package com.googlecode.jsonrpc4j.jsonorg;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.googlecode.jsonrpc4j.JsonEngine;
import com.googlecode.jsonrpc4j.JsonException;
import com.googlecode.jsonrpc4j.JsonRpcError;

public class JsonOrgJsonEngine 
	implements JsonEngine {
	
	private static final String JSON_RPC_VERSION = "2.0";
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	public void addTypeAlias(Class<?> fromType, Class<?> toType)
		throws JsonException {
		// TODO Auto-generated method stub

	}

	public Object createRpcRequest(String methodName, Object[] arguments)
		throws JsonException {
		try {
			JSONObject request = new JSONObject();
			request.put("id", RANDOM.nextInt()+"");
			request.put("jsonrpc", JSON_RPC_VERSION);
			request.put("method", methodName);
			request.put("params", JSONUtil.toJSON(arguments));
			return request;
		} catch(Exception e) {
			throw new JsonException(e);
		}
	}

	public Object createRpcRequest(
		String methodName, Map<String, Object> arguments) 
		throws JsonException {
		try {
			JSONObject request = new JSONObject();
			request.put("id", RANDOM.nextInt()+"");
			request.put("jsonrpc", JSON_RPC_VERSION);
			request.put("method", methodName);
			JSONObject params = new JSONObject();
			for (String name : arguments.keySet()) {
				params.put(name, JSONUtil.toJSON(arguments.get(name)));
			}
			request.put("params", params);
			return request;
		} catch(Exception e) {
			throw new JsonException(e);
		}
	}

	public String getIdFromRpcRequest(Object json) 
		throws JsonException {
		try {
			return ((JSONObject)json).getString("id");
		} catch(Exception e) {
			throw new JsonException(e);
		}
	}

	public JsonRpcError getJsonErrorFromResponse(Object jsonResponse)
		throws JsonException {
		try {
			return JSONUtil.fromJSON(
				((JSONObject)jsonResponse).get("error"), 
				JsonRpcError.class);
		} catch(Exception e) {
			throw new JsonException(e);
		}
	}

	public Object getJsonResultFromResponse(Object jsonResponse)
		throws JsonException {
		try {
			return ((JSONObject)jsonResponse).get("result");
		} catch(Exception e) {
			throw new JsonException(e);
		}
	}

	public String getMethodNameFromRpcRequest(Object json) 
		throws JsonException {
		try {
			return ((JSONObject)json).getString("method");
		} catch(Exception e) {
			throw new JsonException(e);
		}
	}

	public int getParameterCountFromRpcRequest(Object json)
		throws JsonException {
		try {
			JSONObject request = (JSONObject)json;
			if (!request.has("params")) {
				return 0;
			}
			Object params = request.get("params");
			if (params instanceof JSONArray) {
				return ((JSONArray)params).length();
			} else {
				return ((JSONObject)params).length();
			}
		} catch(Exception e) {
			throw new JsonException(e);
		}
	}

	public Object getParameterFromRpcRequest(Object json, int index)
		throws JsonException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getParameterFromRpcRequest(Object json, String name)
			throws JsonException {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<Object> getRpcBatchIterator(Object json)
			throws JsonException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isNotification(Object json) throws JsonException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRpcBatchRequest(Object json) throws JsonException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRpcRequestParametersIndexed(Object json)
			throws JsonException {
		// TODO Auto-generated method stub
		return false;
	}

	public <T> T jsonToObject(Object json, Class<T> valueType)
			throws JsonException {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T jsonToObject(Object json, Type valueType) throws JsonException {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> Object objectToJson(T obj) throws JsonException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object readJson(InputStream in) throws JsonException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object validateRpcBatchRequest(Object json) throws JsonException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object validateRpcRequest(Object json) throws JsonException {
		// TODO Auto-generated method stub
		return null;
	}

	public void writeJson(Object json, OutputStream out) throws JsonException {
		// TODO Auto-generated method stub

	}

}
