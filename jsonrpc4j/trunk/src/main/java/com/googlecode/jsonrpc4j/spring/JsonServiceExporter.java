package com.googlecode.jsonrpc4j.spring;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.HttpRequestHandler;

import com.googlecode.jsonrpc4j.JsonParseException;
import com.googlecode.jsonrpc4j.JsonRpcError;
import com.googlecode.jsonrpc4j.JsonRpcResponse;
import com.googlecode.jsonrpc4j.JsonRpcUtils;

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
    
    private Set<Method> serviceMethods = new HashSet<Method>();
    private boolean strict = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() 
        throws Exception {
        for (Method method : getServiceInterface().getMethods()) {
            serviceMethods.add(method);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, 
        IOException {
        
        // ready the input and output streams
        InputStream jsonInput   = request.getInputStream();
        OutputStream jsonOutput = response.getOutputStream();
        ByteArrayOutputStream jsonOutputBuffer = new ByteArrayOutputStream();
        
        // ready the request and response
        JSONObject rpcRequest = null;
        String id = null;
        JsonRpcResponse rpcResponse = null;
        
        // get content type
        String contentType = request.getContentType().split(";")[0];
        
        // read in the request
        try {
            rpcRequest = JsonRpcUtils.readRequest(jsonInput);
            id = JsonRpcUtils.getId(rpcRequest);
            
        } catch(JsonParseException e) {
            response.setStatus(500);
            rpcResponse = new JsonRpcResponse(null, new JsonRpcError(-32700, 
                "Parse Error", null), null);
        }
        
        // make sure we have the correct content type
        if (strict && 
            !ArrayUtils.contains(JSONRPC_REQUEST_CONTENT_TYPES, contentType)) {
            response.setStatus(400);
            rpcResponse = new JsonRpcResponse(null, new JsonRpcError(-32600, 
                "Invalid Request (Content-Type Header)", null), id);
            
        // check Accept
        } else if (strict && 
            (StringUtils.isEmpty(request.getHeader("Accept"))
                || !ArrayUtils.contains(JSONRPC_REQUEST_CONTENT_TYPES, request.getHeader("Accept")))) {
            response.setStatus(400);
            rpcResponse = new JsonRpcResponse(null, new JsonRpcError(-32600, 
                "Invalid Request (Accept Header)", null), id);
            
        // invoke the service method
        } else if (rpcResponse==null) {
            rpcResponse = invokeServiceMethod(rpcRequest, response);
            
        }
        
        // write responses to the buffer
        jsonOutputBuffer.reset();
        if (!JsonRpcUtils.isNotification(rpcRequest)) {
            JsonRpcUtils.writeResponse(rpcResponse, jsonOutputBuffer);
        }
        response.setContentLength(jsonOutputBuffer.size());
        response.setContentType(JSONRPC_RESPONSE_CONTENT_TYPE);
        jsonOutputBuffer.writeTo(jsonOutput);
        jsonOutput.flush();
        
    }
    
    private JsonRpcResponse invokeServiceMethod(
        JSONObject rpcRequest, HttpServletResponse response) {
        
        String id = JsonRpcUtils.getId(rpcRequest);
        
        // find the methods
        Method[] methods = JsonRpcUtils.getPotentialMethods(
            rpcRequest, serviceMethods.toArray(new Method[0]));
        if (ArrayUtils.isEmpty(methods)) {
            response.setStatus(400);
            return new JsonRpcResponse(null, new JsonRpcError(-32601, 
                  "Method Not Found: "+JsonRpcUtils.getMethod(rpcRequest), null), id);
        }
        
        // find the correct method
        Method serviceMethod = null;
        Object[] parameters = null;
        for (Method sm : methods) {
            parameters = JsonRpcUtils.getParameters(rpcRequest, sm);
            if (parameters!=null) {
               serviceMethod = sm;
               break;
            }
        }
        
        // bail if we didn't find a method
        if (serviceMethod==null || parameters==null) {
            response.setStatus(500);
            return new JsonRpcResponse(null, new JsonRpcError(-32602, 
                "Invalid Parameters", null), id);
        }
        
        // invoke the method
        JsonRpcResponse rpcResponse = null;
        try {
            Object result = ReflectionUtils.invokeMethod(
                serviceMethod, getService(), parameters);
            response.setStatus(200);
            rpcResponse = new JsonRpcResponse(result, null, id);
            
        } catch(Throwable t) {
            response.setStatus(500);
            return new JsonRpcResponse(null, new JsonRpcError(-32603, 
                "Internal Error: "+t.getLocalizedMessage(), null), id);
        }
        
        // create and return response
        return rpcResponse;
    }

    /**
     * @param strict the strict to set
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }
    
}
