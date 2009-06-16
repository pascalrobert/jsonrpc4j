package com.googlecode.jsonrpc4j;

import java.util.List;

public class JsonRpcRequest {
    
    private String jsonrpc;
    private String method;
    private List<Object> params;
    private String id;
    
    /**
     * @return the jsonrpc
     */
    public String getJsonrpc() {
        return jsonrpc;
    }
    
    /**
     * @param jsonrpc the jsonrpc to set
     */
    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }
    
    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }
    
    /**
     * @return the params
     */
    public List<Object> getParams() {
        return params;
    }
    
    /**
     * @param params the params to set
     */
    public void setParams(List<Object> params) {
        this.params = params;
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
