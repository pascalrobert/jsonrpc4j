

var JSONRPC = {
	
	config: {
		asyncAjaxPost: function(options) {
			throw "No registerd hook for JSONRPC.config.asyncAjaxPost";
		},
		errorMessage: function(msg) {
			alert(msg);
			throw msg;
		}
	},
	
	_invalidResponse: function(options) {
		JSONRPC.config.errorMessage(
			"Call failed to "+options.url+"::"+options.rpcRequest.method);
	},
	
	_handleRpcResponse: function(response) {
		
	},
	
	_generateId: function() {
		
	},
	
	call: function(url, method, id, params, successCallback, failureCallback) {
		JSONRPC.config.asyncAjaxPost({
			"url":				url,
			"successCallback":	successCallback,
			"failureCallback":	failureCallback,
			"rpcRequest": {
				"jsonrpc":		"2.0",
				"method":		method,
				"id":			id,
				"params":		params
			}
		});
	},
	
	notify: function(url, method, params, successCallback, failureCallback) {
		JSONRPC.config.asyncAjaxPost({
			"url":				url,
			"successCallback":	successCallback,
			"failureCallback":	failureCallback,
			"rpcRequest": {
				"jsonrpc":		"2.0",
				"method":		method,
				"params":		params
			}
		});
	},
	
	createCallFunction: function(url, method) {
		return new function(params, successCallback, failureCallback) {
			return JSONRPC.call(
				url, 
				method, 
				JSONRPC._generateId(), 
				params,
				successCallback, 
				failureCallback);
		}
	},
	
	createNotifyFunction: function(url, method) {
		return new function(params, successCallback, failureCallback) {
			return JSONRPC.notify(
				url, 
				method,
				params,
				successCallback, 
				failureCallback);
		}
	},
	
	registerPrototypeJs: function() {
		JSONRPC.config.asyncAjaxPost = function(options) {
			return new Ajax.Request(options.url, {
				asynchronous:	true,
				method:			"POST",
				postBody: 		$H(options.rpcRequest).toJSON(),
				contentType:	"application/json",
				requestHeaders: { "Accept": "application/json" },
			  	onSuccess: function(transport) {
					if (!transport.responseText.isJSON()) {
						JSONRPC._invalidResponse(options);
					} else {
						var response = transport.responseText.evalJSON(true);
						JSONRPC._handleRpcResponse(
							response, options.successCallback, options.failureCallback);
					}
				},
				onFailure: function(transport) {
					JSONRPC._invalidResponse(options);
				}
			});
		};
	}
};

