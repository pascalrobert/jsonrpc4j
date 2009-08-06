package com.googlecode.jsonrpc4j.spring;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

import com.googlecode.jsonrpc4j.JsonEngine;
import com.googlecode.jsonrpc4j.JsonRpcError;
import com.googlecode.jsonrpc4j.JsonRpcErrorException;
import com.googlecode.jsonrpc4j.JsonRpcParamName;


public class JsonProxyFactoryBean 
	extends UrlBasedRemoteAccessor 
	implements MethodInterceptor,
	InitializingBean,
	FactoryBean {
	
	private Object proxyObject = null;
	private HttpClient httpClient = null;
	private JsonEngine jsonEngine = null;
	private Map<String, String> extraHttpHeaders = new HashMap<String, String>();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		proxyObject = ProxyFactory.getProxy(getServiceInterface(), this);
		if (httpClient==null) {
			httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object invoke(MethodInvocation invocation) 
		throws Throwable {
		
		// get parameter names and parameters for the invocation
		JsonRpcParamName[] paramNames = getParamNames(invocation.getMethod());
		Object[] arguments = invocation.getArguments();
		
		// the request object
		Object rpcRequest = null;
		
		// build named params request
		if (invocation.getArguments().length>0
			&& paramNames != null) {
			
			// build map
			Map<String, Object> params = new HashMap<String, Object>();
			for (int i=0; i<paramNames.length; i++) {
				params.put(paramNames[i].value(), arguments[i]);
			}
			
			// build request
			rpcRequest = jsonEngine.createRpcRequest(
				invocation.getMethod().getName(), params);
			
		// build indexed params request
		} else {
			
			// build request
			rpcRequest = jsonEngine.createRpcRequest(
				invocation.getMethod().getName(), arguments);
			
		}
		
		// convert request to a string
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		jsonEngine.writeJson(rpcRequest, out);
		out.flush();
		
		// create POST method
		PostMethod method = new PostMethod(getServiceUrl());
		
		// add http headers
		for (String header : extraHttpHeaders.keySet()) {
			method.addRequestHeader(header, extraHttpHeaders.get(header));
		}
		//method.setRequestHeader("User-Agent", this.getClass().getName());
		method.setRequestHeader("Content-Type", "application/json-rpc");
		method.setRequestEntity(new ByteArrayRequestEntity(
			out.toByteArray(), "application/json-rpc"));
		
		// execute the method
		Object rpcResponse = null;
		try {
			httpClient.executeMethod(method);
			if (method.getStatusCode()>=300) {
				throw new HttpException(					
					"Did not receive successful HTTP response: status code = " 
					+ method.getStatusCode() 
					+", status message = [" + method.getStatusText() + "]");
			}
			rpcResponse = jsonEngine.readJson(method.getResponseBodyAsStream());
		} finally {
			method.releaseConnection();
		}
		
		// check for errors
		JsonRpcError error = jsonEngine.getJsonErrorFromResponse(rpcResponse);
		if (error!=null) {
			throw new JsonRpcErrorException(error);
		}
		
		// read result
		if (invocation.getMethod().getReturnType()!=null) {
			Object result = jsonEngine.getJsonResultFromResponse(rpcResponse);
			return jsonEngine.jsonToObject(result, invocation.getMethod().getReturnType());
		} else {
			return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getObject() 
		throws Exception {
		return proxyObject;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Class<?> getObjectType() {
		return getServiceInterface();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isSingleton() {
		return true;
	}
	
	/**
	 * Returns the {@link JsonRpcParamName} for a given {@link Method}.
	 * @param method the method
	 * @return the {@code JsonRpcParamName}s
	 */
	private JsonRpcParamName[] getParamNames(Method method) {
		JsonRpcParamName[] ret = new JsonRpcParamName[method.getParameterTypes().length];
		for (int i=0; i<ret.length; i++) {
			Annotation[] annotations = method.getParameterAnnotations()[i];
			for (int j=0; j<annotations.length; j++) {
				if (annotations[j] instanceof JsonRpcParamName) {
					ret[i] = (JsonRpcParamName)annotations[j];
					break;
				}
			}
			if (ret[i]==null) {
				return null;
			}
		}
		return ret;
	}

	/**
	 * @param httpClient the httpClient to set
	 */
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * @param jsonEngine the jsonEngine to set
	 */
	public void setJsonEngine(JsonEngine jsonEngine) {
		this.jsonEngine = jsonEngine;
	}

	/**
	 * @param extraHttpHeaders the extraHttpHeaders to set
	 */
	public void setExtraHttpHeaders(Map<String, String> extraHttpHeaders) {
		this.extraHttpHeaders = extraHttpHeaders;
	}

}