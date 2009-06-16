package com.googlecode.jsonrpc4j;

/**
 * <a href="http://groups.google.com/group/json-rpc/web/json-rpc-1-2-proposal">
 * http://groups.google.com/group/json-rpc/web/json-rpc-1-2-proposal</a>
 * 
 * @author brian.dilley@gmail.com
 *
 */
public class JsonRpcResponse {
    
    private Object result;
    private JsonRpcError error;
    private String id;
    
    public JsonRpcResponse(Object result, JsonRpcError error, String id) {
        this.result = result;
        this.error  = error;
        this.id     = id;
    }
    
    public JsonRpcResponse() {
        this(null, null, null);
    }
    
    /**
     * @return the result
     */
    public Object getResult() {
        return result;
    }
    
    /**
     * @param result the result to set
     */
    public void setResult(Object result) {
        this.result = result;
    }
    
    /**
     * @return the error
     */
    public JsonRpcError getError() {
        return error;
    }
    
    /**
     * @param error the error to set
     */
    public void setError(JsonRpcError error) {
        this.error = error;
    }
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
}
