package com.googlecode.jsonrpc4j;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonRpcClientTest {

	private ByteArrayOutputStream baos;
	private JsonRpcClient client;

	@Before
	public void setUp() {
		client = new JsonRpcClient();
		baos = new ByteArrayOutputStream();
	}

	@After
	public void tearDown() {
		client = null;
	}

	private JsonNode readJSON(ByteArrayOutputStream baos)
		throws JsonProcessingException,
		IOException {
		return client.getObjectMapper().readTree(baos.toString());
	}

	@Test
	public void testInvokeNoParams()
		throws Throwable {
		
		client.invoke("test", new Object[0], baos);
		JsonNode node = readJSON(baos);
		assertFalse(node.has("params"));

		client.invoke("test", (Object[])null, baos);
		node = readJSON(baos);
		assertFalse(node.has("params"));
	}

	@Test
	public void testInvokeArrayParams()
		throws Throwable {
		client.invoke("test", new Object[] { 1, 2 }, baos);
		JsonNode node = readJSON(baos);

		assertTrue(node.has("params"));
		assertTrue(node.get("params").isArray());
		assertEquals(1, node.get("params").get(0).getIntValue());
		assertEquals(2, node.get("params").get(1).getIntValue());
	}

	@Test
	public void testInvokeHashParams()
		throws Throwable {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("hello", "Guvna");
		params.put("x", 1);
		client.invoke("test", params, baos);
		JsonNode node = readJSON(baos);

		assertTrue(node.has("params"));
		assertTrue(node.get("params").isObject());
		assertEquals("Guvna", node.get("params").get("hello").getTextValue());
		assertEquals(1, node.get("params").get("x").getIntValue());
	}

}
