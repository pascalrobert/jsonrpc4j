package com.googlecode.jsonrpc4j;

/**
 * Thrown on error (TODO: make this comment un-suck)
 * 
 * @author brian.dilley@gmail.com
 *
 */
@SuppressWarnings("serial")
public class JsonRpcErrorException 
    extends Exception {
    
	private JsonRpcError error;
	
    public JsonRpcErrorException(JsonRpcError error) {
        super(error.getMessage());
        this.error = error;
    }

	/**
	 * @return the error
	 */
	public JsonRpcError getError() {
		return error;
	}
    
}
