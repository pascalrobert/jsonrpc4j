package com.googlecode.jsonrpc4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class JsonRpcUtils {
    
    /**
     * Reads a JSON object from the given input stream and
     * ensures that it's a json-rpc request.
     * @param input
     * @return
     * @throws JsonParseException
     */
    public static JSONObject readRequest(InputStream input)
        throws JsonParseException {
        try {
            return JSONObject.fromObject(IOUtils.toString(input));
        } catch(Exception e) {
            throw new JsonParseException();
        }
    }
    
    /**
     * Writes the given JsonRpcResponse to the output stream.
     * @param response
     * @param output
     * @throws IOException
     */
    public static void writeResponse(
        JsonRpcResponse response, OutputStream output)
        throws IOException {
        IOUtils.write(JSONObject.fromObject(response).toString(), output);
    }
    
    /**
     * Returns the rpc method.
     * @param rpcRequest the json object
     * @return the method
     */
    public static String getMethod(JSONObject rpcRequest) {
        return rpcRequest.getString("method");
    }
    
    /**
     * Returns the rpc id.
     * @param rpcRequest the json object
     * @return the method
     */
    public static String getId(JSONObject rpcRequest) {
        return isNotification(rpcRequest) ? null : rpcRequest.getString("id");
    }
    
    /**
     * Returns the number of params in the given rpc request.
     * @param rpcRequest the json object
     * @return the method
     */
    public static int getParamCount(JSONObject rpcRequest) {
        return isParamsIndexed(rpcRequest)
            ? rpcRequest.getJSONArray("params").size()
            : rpcRequest.getJSONObject("params").size();
    }
    
    /**
     * Checks to see if the given rpc request has indexed params
     * vs. named params.
     * @param rpcRequest the json object
     * @return the method
     */
    public static boolean isParamsIndexed(JSONObject rpcRequest) {
        return rpcRequest.get("params").getClass().isAssignableFrom(JSONArray.class);
    }
    
    /**
     * Checks to see if the given rpc request is a notification.
     * @param rpcRequest the json object
     * @return the method
     */
    public static boolean isNotification(JSONObject rpcRequest) {
        return !rpcRequest.containsKey("id") 
            && StringUtils.isEmpty(rpcRequest.getString("id"));
    }
    
    /**
     * Returns the rpc param by it's index as the given type.
     * @param rpcRequest the json object
     * @return the method
     */
    public static Object getParamByIndex(JSONObject rpcRequest, int index, Class<?> type) {
        return convertType(rpcRequest.getJSONArray("params").get(index), type);
    }
    
    /**
     * Returns the rpc param by it's name as the given type.
     * @param rpcRequest the json object
     * @return the method
     */
    public static Object getParamByName(JSONObject rpcRequest, String name, Class<?> type) {
        return convertType(rpcRequest.getJSONObject("params").get(name), type);
    }

    /**
     * Converts the given value to the given type.
     * @param value
     * @param type
     * @return
     */
    public static Object convertType(Object value, Class<?> type) {
    	
    	Class<?> componentType = (type.isArray())
    		? type.getComponentType() : type;
        
    	// convert arrays
    	if (value.getClass().equals(type)) {
    		return value;
    		
    	// JSONNull	
    	} else if (value==null || value.getClass().isAssignableFrom(JSONNull.class)) {
            return null;
            
        // JSONObject
        } if (value.getClass().isAssignableFrom(JSONObject.class)) {
            return JSONObject.toBean((JSONObject)value, type);
            
        // JSONArray
        } else if (value.getClass().isAssignableFrom(JSONArray.class)) {
            JSONArray jsonArray = (JSONArray)value;
            Object values = Array.newInstance(componentType, jsonArray.size());
            for (int i=0; i<jsonArray.size(); i++) {
            	Array.set(values, i, convertType(jsonArray.get(i), componentType));
            }
            return values;
            
        // convert arrays
    	} else if (type.isArray()) {
			Object[] values = (Object[])value;
			Object returnValues = Array.newInstance(componentType, values.length);
			for (int i=0; i<values.length; i++) {
            	Array.set(returnValues, i, convertType(values[i], componentType));
			}
			return returnValues;
            
        // others
        } else {
        	
        	// get string value
        	String stringValue = value.toString();
        	
        	// convert
        	if (type.equals(boolean.class)) {
        		return Boolean.parseBoolean(stringValue);
        	} else if (type.equals(Boolean.class)) {
        		return new Boolean(stringValue);
        		
        	} else if (type.equals(short.class)) {
        		return Short.parseShort(stringValue);
        	} else if (type.equals(Short.class)) {
        		return new Short(stringValue);
        		
        	} else if (type.equals(int.class)) {
        		return (int)Integer.parseInt(stringValue);
        	} else if (type.equals(Integer.class)) {
        		return new Integer(stringValue);
        		
        	} else if (type.equals(long.class)) {
        		return Long.parseLong(stringValue);
        	} else if (type.equals(Long.class)) {
        		return new Long(stringValue);
        		
        	} else if (type.equals(double.class)) {
        		return Double.parseDouble(stringValue);
        	} else if (type.equals(Double.class)) {
        		return new Double(stringValue);
        		
        	} else if (type.equals(float.class)) {
        		return Float.parseFloat(stringValue);
        	} else if (type.equals(Float.class)) {
        		return new Float(stringValue);
        		
        	} else if (type.equals(byte.class)) {
        		return Byte.parseByte(stringValue);
        	} else if (type.equals(Byte.class)) {
        		return new Byte(stringValue);
        		
        	} else if (type.equals(char.class)) {
        		return stringValue.charAt(0);
        	} else if (type.equals(Character.class)) {
        		return new Character(stringValue.charAt(0));
        		
        	} else if (type.equals(BigDecimal.class)) {
        		return new BigDecimal(stringValue);
        		
        	} else if (type.equals(BigInteger.class)) {
        		return new BigInteger(stringValue);
    		
        	}
        }
        
        // wtf?
        throw new IllegalArgumentException(
            "Don't know how to convert "+value.getClass().getName()+" to "+type.getName());
    }
    
    /**
     * Finds potential methods for the request.  Potential
     * methods are methods with the same name and number 
     * of arguments.
     * @param rpcRequest the request
     * @param serviceMethods the service methods
     * @return the methods
     */
    public static Method[] getPotentialMethods(
        JSONObject rpcRequest, Method[] serviceMethods) {
        Set<Method> methods = new HashSet<Method>();
        for (Method m : serviceMethods) {
            if (m.getName().equals(getMethod(rpcRequest))) {
                methods.add(m);
            }
        }
        return methods.toArray(new Method[0]);
    }
    
    /**
     * Reflects an annotated parameter name from the method.
     * @param method the method
     * @param index the parameter index
     * @return the parameter name
     */
    public static String getParamName(Method method, int index) {
        Annotation[][] annotations = method.getParameterAnnotations();
        if (index>=annotations.length) {
            return null;
        }
        for (Annotation annotation : annotations[index]) {
            if (annotation instanceof JsonRpcParamName) {
                return ((JsonRpcParamName)annotation).value();
            }
        }
        return null;
    }
    
    /**
     * Returns the method parameters from the given json-rpc
     * request as they would be applied to the given {@link Method}.
     * @param rpcRequest
     * @param method
     * @return
     */
    public static Object[] getParameters(
        JSONObject rpcRequest, Method method) {
        
        // continue to next method if the param
        // counts don't match
        Class<?>[] methodParamTypes = method.getParameterTypes();
        if (methodParamTypes.length!=getParamCount(rpcRequest)) {
            return null;
            
        // if the param lengths are 0 on both then
        // we've found our match
        } else if (methodParamTypes.length==0 && getParamCount(rpcRequest)==0) {
            return new Object[0];
        }
        
        // get parameter types from the rpc request
        Class<?>[] rpcParamTypes = new Class<?>[getParamCount(rpcRequest)];
        Object[] rpcParamValues = new Object[getParamCount(rpcRequest)];
        
        // indexed params
        if (isParamsIndexed(rpcRequest)) {
            for (int i=0; i<rpcParamTypes.length; i++) {
                rpcParamValues[i] = getParamByIndex(rpcRequest, i, methodParamTypes[i]);
                rpcParamTypes[i] = rpcParamValues[i].getClass();
            }
            
        // named params
        } else {
            
            // reflect parameter annotations
            for (int i=0; i<rpcParamTypes.length; i++) {
                
                // get param name
                String paramName = getParamName(method, i);
                if (paramName==null) {
                    continue;
                }
                
                // get param value
                rpcParamValues[i] = getParamByName(rpcRequest, paramName, methodParamTypes[i]);
                rpcParamTypes[i] = rpcParamValues[i].getClass();
            }
        }
        
        // make sure the types align
        for (int i=0; i<methodParamTypes.length; i++) {
        	
        	// get the actual (non array) types
        	Class<?> methodType = methodParamTypes[i].isArray()
    			? methodParamTypes[i].getComponentType() : methodParamTypes[i];
        	Class<?> rpcType = rpcParamTypes[i].isArray()
    			? rpcParamTypes[i].getComponentType() : rpcParamTypes[i];
        		
    		// easy if it's not a primitive
        	if (!methodType.isPrimitive() 
        		&& !methodType.isAssignableFrom(rpcType)) {
        		return null;
        		
        	// check the primitive types
        	} else if (methodType.isPrimitive()) {
        		if (methodType.equals(short.class)
        			&& !(Short.class.isAssignableFrom(rpcType) ||
        				short.class.isAssignableFrom(rpcType))) {
        			return null;
        			
        		} else if (methodType.equals(int.class)
        			&& !(Integer.class.isAssignableFrom(rpcType) ||
        				int.class.isAssignableFrom(rpcType))) {
        			return null;
        			
                } else if (methodType.equals(long.class)
                	&& !(Long.class.isAssignableFrom(rpcType) ||
                		long.class.isAssignableFrom(rpcType))) {
        			return null;
        			
                } else if (methodType.equals(float.class)
                	&& !(Float.class.isAssignableFrom(rpcType) ||
                		float.class.isAssignableFrom(rpcType))) {
        			return null;
        			
                } else if (methodType.equals(double.class)
                	&& !(Double.class.isAssignableFrom(rpcType) ||
                		double.class.isAssignableFrom(rpcType))) {
        			return null;
        			
                } else if (methodType.equals(boolean.class)
                	&& !(Boolean.class.isAssignableFrom(rpcType) ||
                		boolean.class.isAssignableFrom(rpcType))) {
        			return null;
        			
                } else if (methodType.equals(char.class)
                	&& !(Character.class.isAssignableFrom(rpcType) ||
                		char.class.isAssignableFrom(rpcType))) {
        			return null;
        			
                } else if (methodType.equals(byte.class)
                	&& !(Byte.class.isAssignableFrom(rpcType) ||
                		byte.class.isAssignableFrom(rpcType))) {
        			return null;
                }
        	}
        }
        
        // return the values
        return rpcParamValues;
    }
    
}
