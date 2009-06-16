package com.googlecode.jsonrpc4j.spring;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.googlecode.jsonrpc4j.jackson.JacksonJsonEngine;

public class JacksonJsonEngineFactoryBean 
	extends AbstractFactoryBean {

	private Map<Class<?>, Class<?>> typeAliases = new HashMap<Class<?>, Class<?>>();
	
	@SuppressWarnings("unchecked")
	private Map<Class, JsonSerializer> jsonSerializers = new HashMap<Class, JsonSerializer>();
	
	@SuppressWarnings("unchecked")
	private Map<Class, JsonDeserializer> jsonDeserializers = new HashMap<Class, JsonDeserializer>();
	
	@Override
	@SuppressWarnings("unchecked")
	protected Object createInstance() 
		throws Exception {
		JacksonJsonEngine engine = new JacksonJsonEngine();
		for (Class<?> key : typeAliases.keySet()) {
			engine.addTypeAlias(key, typeAliases.get(key));
		}
		for (Class key : jsonSerializers.keySet()) {
			engine.addJsonSerializer(key, jsonSerializers.get(key));
		}
		for (Class key : jsonDeserializers.keySet()) {
			engine.addJsonDeserializer(key, jsonDeserializers.get(key));
		}
		return engine;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return JacksonJsonEngine.class;
	}
	
	@SuppressWarnings("unchecked")
	public void setJsonDeserializers(Map<Class, JsonDeserializer> jsonDeserializers) {
		this.jsonDeserializers.putAll(jsonDeserializers);
	}
	
	@SuppressWarnings("unchecked")
	public void setJsonSerializers(Map<Class, JsonSerializer> jsonSerializers) {
		this.jsonSerializers.putAll(jsonSerializers);
	}
	
	public void setTypeAliases(Map<Class<?>, Class<?>> typeAliases) {
		this.typeAliases.putAll(typeAliases);
	}

}
