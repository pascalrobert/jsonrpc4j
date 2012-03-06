package com.googlecode.jsonrpc4j;

import org.codehaus.jackson.node.ObjectNode;

/**
 * Resolves client {@link Throwable}s from server
 * generated {@link ObjectNode}.
 *
 */
public interface ExceptionResolver {

	/**
	 * Resolves the exception from the given json-rpc
	 * response {@link ObjectNode}.
	 * @param response the response
	 * @return the exception
	 */
	Throwable resolveException(ObjectNode response);

}
