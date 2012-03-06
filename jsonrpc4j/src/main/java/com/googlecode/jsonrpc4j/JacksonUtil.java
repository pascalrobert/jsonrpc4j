package com.googlecode.jsonrpc4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

public abstract class JacksonUtil {

	/**
	 * Writes and flushes the given {@code value} to the
	 * {@link OutputStream} using the given {@link ObjectMapper}
	 * and prevents Jackson from closing the {@link OutputStream}.
	 * 
	 * @param mapper
	 * @param ops
	 * @param value
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static void writeValue(ObjectMapper mapper, OutputStream ops, Object value)
		throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(new NoCloseOutputStream(ops), value);
		ops.flush();
	}

	/**
	 * Reads a Json tree using the given {@link ObjectMapper} from the
	 * given {@link InputStream} and prevents Jackson from closing the
	 * {@link InputStream}.
	 * 
	 * @param mapper
	 * @param ips
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static JsonNode readTree(ObjectMapper mapper, InputStream ips)
		throws JsonParseException, JsonMappingException, IOException {
		return mapper.readTree(new NoCloseInputStream(ips));
	}

	/**
	 * Reads a Json tree using the given {@link ObjectMapper} from the
	 * given {@link InputStream} and prevents Jackson from closing the
	 * {@link InputStream}.
	 * 
	 * @param mapper
	 * @param ips
	 * @param valueType
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static JsonNode readValue(ObjectMapper mapper, InputStream ips, JavaType valueType)
		throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(new NoCloseInputStream(ips), valueType);
	}

}
