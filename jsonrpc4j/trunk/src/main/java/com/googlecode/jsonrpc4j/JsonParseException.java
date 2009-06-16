package com.googlecode.jsonrpc4j;

/**
 * Thrown when JSON data couldn't be parsed.
 * 
 * @author brian.dilley@gmail.com
 *
 */
@SuppressWarnings("serial")
public class JsonParseException 
    extends Exception {
    
    public JsonParseException(String message) {
        super(message);
    }
    
    public JsonParseException(Throwable rootCause) {
        super(rootCause);
    }
    
    public JsonParseException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
    
}
