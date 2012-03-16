package com.googlecode.jsonrpc4j.spring;

import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;

import org.springframework.beans.factory.DisposableBean;

import com.googlecode.jsonrpc4j.StreamServer;

/**
 * {@link RemoteExporter} that exports services using Json
 * according to the JSON-RPC proposal specified at:
 * <a href="http://groups.google.com/group/json-rpc">
 * http://groups.google.com/group/json-rpc</a>.
 *
 */
public class JsonStreamServiceExporter
	extends AbstractJsonServiceExporter
	implements DisposableBean {

	public static final int DEFAULT_MAX_THREADS		= 50;
	public static final int DEFAULT_PORT			= 10420;
	public static final int DEFAULT_BACKLOG			= 0;
	public static final String DEFAULT_HOSTNAME		= "0.0.0.0";

	private ServerSocketFactory serverSocketFactory;
	private int maxThreads		= DEFAULT_MAX_THREADS;
	private int port			= DEFAULT_PORT;
	private int backlog			= DEFAULT_BACKLOG;
	private String hostName		= DEFAULT_HOSTNAME;
	
	private StreamServer streamServer;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void exportService()
		throws Exception {

		// create a stream server if needed
		if (streamServer==null) {
	
			// make sure we have a factory
			if (serverSocketFactory==null) {
				serverSocketFactory = ServerSocketFactory.getDefault();
			}
	
			// create server socket
			ServerSocket serverSocket = serverSocketFactory
				.createServerSocket(port, backlog, InetAddress.getByName(hostName));
	
			// create the stream server
			streamServer = new StreamServer(getJsonRpcServer(), maxThreads, serverSocket);
		}

		// start it
		streamServer.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy()
		throws Exception {
		streamServer.stop();
	}

	/**
	 * @param serverSocketFactory the serverSocketFactory to set
	 */
	protected void setServerSocketFactory(ServerSocketFactory serverSocketFactory) {
		this.serverSocketFactory = serverSocketFactory;
	}

	/**
	 * @param maxThreads the maxThreads to set
	 */
	protected void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	/**
	 * @param port the port to set
	 */
	protected void setPort(int port) {
		this.port = port;
	}

	/**
	 * @param backlog the backlog to set
	 */
	protected void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	/**
	 * @param hostName the hostName to set
	 */
	protected void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @param streamServer the streamServer to set
	 */
	protected void setStreamServer(StreamServer streamServer) {
		this.streamServer = streamServer;
	}

}
