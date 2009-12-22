package com.googlecode.jsonrpc4j.jsonorg.converters;

import com.googlecode.jsonrpc4j.jsonorg.TypeConverter;

/**
 * a {@link TypeConverter} for primitives.
 *
 */
public class PrimitiveTypeConverter 
	implements TypeConverter {

	/**
	 * {@inheritDoc}
	 */
	public Object fromJSON(Object object, Class<?> clazz) {
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object toJSON(Object object, Class<?> clazz) {
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean supports(Class<?> clazz) {
		return clazz.isPrimitive()
			|| Boolean.class.isAssignableFrom(clazz)
			|| Number.class.isAssignableFrom(clazz)
			|| String.class.isAssignableFrom(clazz);
	}

}
