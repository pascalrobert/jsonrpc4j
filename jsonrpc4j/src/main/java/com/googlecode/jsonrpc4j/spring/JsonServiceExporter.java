package com.googlecode.jsonrpc4j.spring;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.HttpRequestHandler;

import com.googlecode.jsonrpc4j.JsonEngine;
import com.googlecode.jsonrpc4j.JsonException;
import com.googlecode.jsonrpc4j.JsonRpcParamName;
import com.googlecode.jsonrpc4j.JsonRpcResponse;

/**
 * {@link RemoteExporter} that exports services using Json
 * according to the JSON-RPC proposal specified at:
 * <a href="http://groups.google.com/group/json-rpc">
 * http://groups.google.com/group/json-rpc</a>.
 * 
 * @author brian.dilley@gmail.com
 *
 */
public class JsonServiceExporter 
    extends RemoteExporter 
    implements HttpRequestHandler,
    InitializingBean {
    
    public static final String JSONRPC_RESPONSE_CONTENT_TYPE = "application/json-rpc";
    public static final String[] JSONRPC_REQUEST_CONTENT_TYPES = {
        "application/json-rpc",
        "application/json",
        "application/jsonrequest"
    };
    
    private Set<Method> serviceInterfaceMethods = new HashSet<Method>();
    private Map<Method, Method> serviceImplMethods = new HashMap<Method, Method>();
    private JsonEngine jsonEngine;

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() 
        throws Exception {
        for (Method method : getServiceInterface().getMethods()) {
            serviceInterfaceMethods.add(method);
            serviceImplMethods.put(method, getService().getClass().getMethod(
            	method.getName(), method.getParameterTypes()));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, 
        IOException {
        
        // ready the request and response
        Object rpcRequest = null;
        List<JsonRpcResponse> responses = new ArrayList<JsonRpcResponse>();
        
        // read in the request
        try {
            rpcRequest = jsonEngine.readJson(request.getInputStream());
        } catch(JsonException e) {
            response.setStatus(500);
            responses.add(JsonRpcResponse.createError(-32700, "Parse Error", null, null));
        }
        
        // invoke
        if (rpcRequest!=null) {
	        try {
	        	responses.addAll(handleRpcRequest(rpcRequest, response));
	        } catch(Exception e) {
	        	throw new ServletException(e);
	        }
        }
        
        // convert response to json
        Object jsonResponse = null;
        try {
	        if (rpcRequest!=null && jsonEngine.isRpcBatchRequest(rpcRequest)) {
	        	jsonResponse = jsonEngine.objectToJson(responses);
	        } else if (responses.size()==1) {
	        	jsonResponse = jsonEngine.objectToJson(responses.get(0));
	        }
        } catch(Exception e) {
        	throw new ServletException(e);
        }
        
        
        // write response
        try {
	        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
	        if (jsonResponse!=null) {
	        	jsonEngine.writeJson(jsonResponse, outBuffer);
	        }
	        response.setContentLength(outBuffer.size());
	        response.setContentType(JSONRPC_RESPONSE_CONTENT_TYPE);
	        outBuffer.writeTo(response.getOutputStream());
	        response.getOutputStream().flush();
        } catch(Exception e) {
        	throw new ServletException(e);
        }
        
        
    }
    
    /**
     * Handles the given JSON-RPC request, including
     * batch requests.
     * @param rpcRequest
     * @param response
     * @return
     * @throws JsonException
     */
    private List<JsonRpcResponse> handleRpcRequest(
    	Object rpcRequest, HttpServletResponse response) 
    	throws JsonException {
    	
    	// ready responses
    	List<JsonRpcResponse> responses = new ArrayList<JsonRpcResponse>();
    	
    	// handle batch
    	if (jsonEngine.isRpcBatchRequest(rpcRequest)) {
    		Iterator<Object> rpcRequets = jsonEngine.getRpcBatchIterator(rpcRequest);
    		while (rpcRequets.hasNext()) {
    			JsonRpcResponse resp = handleSingleRequest(rpcRequets.next(), response);
    			if (resp!=null) { responses.add(resp); }
    		}
    		
    	// handle single
    	} else {
			JsonRpcResponse resp = handleSingleRequest(rpcRequest, response);
			if (resp!=null) { responses.add(resp); }
    	}
    	
    	// return responses
    	return responses;
    }
    
    /**
     * Handles a single request
     * @param rpcRequest
     * @param response
     * @return
     * @throws JsonException
     */
    private JsonRpcResponse handleSingleRequest(
    	Object rpcRequest, HttpServletResponse response) 
    	throws JsonException {
    	
    	// get the request method and id
    	String requestMethod = jsonEngine.getMethodNameFromRpcRequest(rpcRequest);
    	String requestId = !jsonEngine.isNotification(rpcRequest) 
    		? jsonEngine.getIdFromRpcRequest(rpcRequest) : null;
        
        // find matching method names
        Set<Method> methods = new HashSet<Method>();
        int jsonParameterCount = jsonEngine.getRpcRequestParameterCount(rpcRequest);
        for (Method method : serviceInterfaceMethods) {
        	if (method.getName().equals(requestMethod)
        		&& method.getParameterTypes().length==jsonParameterCount) {
        		methods.add(method);
        	}
        }
        if (methods.size()==0) {
        	return JsonRpcResponse.createError(
        		-32601, "Method Not Found: "+requestMethod, null, requestId);
        }
        
        // find a method with matching parameter types
        MethodAndParams invocation = jsonEngine.isRpcRequestParametersIndexed(rpcRequest)
        	? findIndexedMethodAndParams(methods, rpcRequest)
        	: findNamedMethodAndParams(methods, rpcRequest);
        
        // bail if we didn't find a method
        if (invocation==null) {
        	return JsonRpcResponse.createError(
            	-32602, "Invalid Parameters", null, requestId);
        }
        
        // invoke the method
        try {
            Object result = ReflectionUtils.invokeMethod(
            	invocation.method, getService(), invocation.params.toArray(new Object[0]));
            return (!jsonEngine.isNotification(rpcRequest))
            	? JsonRpcResponse.createResponse(result, requestId) : null;
        } catch(Throwable t) {
        	return JsonRpcResponse.createError(
                -32603, "Internal Error: "+t.getLocalizedMessage(), null, requestId);
        }
    }
    

    /**
     * Finds methods that match the request with indexed params.
     * @param methods
     * @param rpcRequest
     * @return
     * @throws JsonException
     */
    private MethodAndParams findIndexedMethodAndParams(Set<Method> methods, Object rpcRequest)
    	throws JsonException {
    	
    	MethodAndParams ret = new MethodAndParams();
    	
        // loop through each method and check them out
        for (Method method : methods) {
        	
        	// get the types that the method takes
        	Class<?>[] paramTypes = method.getParameterTypes();
        	ret.params.clear();
        		
    		// check for matching types
    		boolean typesMatch = true;
    		for (int i=0; i<paramTypes.length; i++) {
    			try {
    				Object jsonParm = jsonEngine.getRpcRequestParameter(rpcRequest, i);
    				ret.params.add(jsonEngine.jsonToObject(jsonParm, paramTypes[i]));
    			} catch(Exception e) {
    				typesMatch = false;
    				break;
    			}
    		}
    		
    		// we found our method
    		if (typesMatch) {
    			ret.method = method;
    			break;
    		}
        }
        
        // return it
        return (ret.method!=null) ? ret : null;
    }
    
    /**
     * Finds methods that match the request with named params.
     * @param methods
     * @param rpcRequest
     * @return
     * @throws JsonException
     */
    private MethodAndParams findNamedMethodAndParams(Set<Method> methods, Object rpcRequest)
    	throws JsonException {
    	
    	MethodAndParams ret = new MethodAndParams();
    	
        // loop through each method and check them out
        for (Method method : methods) {
        	
        	// get the types that the method takes
        	Class<?>[] paramTypes = method.getParameterTypes();
        	Annotation[][] interfaceAnnotations = method.getParameterAnnotations();
        	Annotation[][] serviceAnnotations = serviceImplMethods.get(method).getParameterAnnotations();
        	ret.params.clear();
        		
    		// check for matching types
    		boolean typesMatch = true;
    		for (int i=0; i<paramTypes.length; i++) {
    			try {
    				// get the param name
    				String paramName = getParamNameByAnnotation(serviceAnnotations[i]);
    				if (paramName==null) {
    					paramName = getParamNameByAnnotation(interfaceAnnotations[i]);
    				}
    				if (paramName==null) {
        				typesMatch = false;
        				break;
    				}
    				
    				Object jsonParm = jsonEngine.getRpcRequestParameter(rpcRequest, paramName);
    				ret.params.add(jsonEngine.jsonToObject(jsonParm, paramTypes[i]));
    			} catch(Exception e) {
    				typesMatch = false;
    				break;
    			}
    		}
    		
    		// we found our method
    		if (typesMatch) {
    			ret.method = method;
    			break;
    		}
        }
        
        // return it
        return (ret.method!=null) ? ret : null;
    }
    
    /**
     * Returns the annotated param name.
     * @param annotations
     * @return
     */
    private String getParamNameByAnnotation(Annotation[] annotations) {
    	for (int i=0; i<annotations.length; i++) {
    		if (annotations[i] instanceof JsonRpcParamName) {
    			return((JsonRpcParamName)annotations[i]).value();
    		}
    	}
    	return null;
    }

	/**
	 * @param jsonEngine the jsonEngine to set
	 */
	public void setJsonEngine(JsonEngine jsonEngine) {
		this.jsonEngine = jsonEngine;
	}
    
    private class MethodAndParams {
    	private Method method;
    	private List<Object> params = new ArrayList<Object>();
    }
    
}
