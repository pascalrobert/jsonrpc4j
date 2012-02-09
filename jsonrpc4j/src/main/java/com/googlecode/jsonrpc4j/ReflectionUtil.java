package com.googlecode.jsonrpc4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities for reflection.
 */
public class ReflectionUtil {

	private static Map<String, Set<Method>> METHOD_CACHE
		= new HashMap<String, Set<Method>>();

	private static Map<Method, List<Class<?>>> PARAMETER_TYPES_CACHE
		= new HashMap<Method, List<Class<?>>>();

	private static Map<Method, List<Annotation>> METHOD_ANNOTATION_CACHE
		= new HashMap<Method, List<Annotation>>();

	private static Map<Method, List<List<Annotation>>> METHOD_PARAM_ANNOTATION_CACHE
		= new HashMap<Method, List<List<Annotation>>>();

	private ReflectionUtil() { }
	static { new ReflectionUtil(); }

	/**
	 * Finds methods with the given name on the given class.
	 * @param clazz the class
	 * @param name the method name
	 * @return the methods
	 */
	public static Set<Method> findMethods(Class<?> clazz, String name) {
		String cacheKey = clazz.getName().concat("::").concat(name);
		if (METHOD_CACHE.containsKey(cacheKey)) {
			return METHOD_CACHE.get(cacheKey);
		}
		Set<Method> methods = new HashSet<Method>();
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(name)) {
				methods.add(method);
			}
		}
		methods = Collections.unmodifiableSet(methods);
		METHOD_CACHE.put(cacheKey, methods);
		return methods;
	}

	/**
	 * Finds methods with the given name on the given class.
	 * @param clazz the class
	 * @param name the method name
	 * @return the methods
	 */
	public static List<Class<?>> getParameterTypes(Method method) {
		if (PARAMETER_TYPES_CACHE.containsKey(method)) {
			return PARAMETER_TYPES_CACHE.get(method);
		}
		List<Class<?>> types = new ArrayList<Class<?>>();
		for (Class<?> type : method.getParameterTypes()) {
			types.add(type);
		}
		types = Collections.unmodifiableList(types);
		PARAMETER_TYPES_CACHE.put(method, types);
		return types;
	}

	/**
	 * Finds methods with the given name on the given class.
	 * @param clazz the class
	 * @param name the method name
	 * @return the methods
	 */
	public static List<Annotation> getAnnotations(Method method) {
		if (METHOD_ANNOTATION_CACHE.containsKey(method)) {
			return METHOD_ANNOTATION_CACHE.get(method);
		}
		List<Annotation> annotations = new ArrayList<Annotation>();
		for (Annotation a : method.getAnnotations()) {
			annotations.add(a);
		}
		annotations = Collections.unmodifiableList(annotations);
		METHOD_ANNOTATION_CACHE.put(method, annotations);
		return annotations;
	}

	/**
	 * Finds methods with the given name on the given class.
	 * @param <T> the annotation type
	 * @param clazz the class
	 * @param name the method name
	 * @return the methods
	 */
	public static <T extends Annotation> List<T> getAnnotations(Method method, Class<T> type) {
		List<T> ret = new ArrayList<T>();
		for (Annotation a : getAnnotations(method)) {
			if (type.isInstance(a)) {
				ret.add(type.cast(a));
			}
		}
		return ret;
	}

	/**
	 * Finds a single annotation on a method.
	 * @param <T> the type
	 * @param method the method
	 * @param type the type of annotation
	 * @return the annotation or null
	 */
	public static <T extends Annotation> T getAnnotation(Method method, Class<T> type) {
		for (Annotation a : getAnnotations(method)) {
			if (type.isInstance(a)) {
				return type.cast(a);
			}
		}
		return null;
	}

	/**
	 * Finds methods with the given name on the given class.
	 * @param clazz the class
	 * @param name the method name
	 * @return the methods
	 */
	public static List<List<Annotation>> getParameterAnnotations(Method method) {
		if (METHOD_PARAM_ANNOTATION_CACHE.containsKey(method)) {
			return METHOD_PARAM_ANNOTATION_CACHE.get(method);
		}
		List<List<Annotation>> annotations = new ArrayList<List<Annotation>>();
		for (Annotation[] paramAnnotations : method.getParameterAnnotations()) {
			List<Annotation> listAnnotations = new ArrayList<Annotation>();
			for (Annotation a : paramAnnotations) {
				listAnnotations.add(a);
			}
			annotations.add(listAnnotations);
		}
		annotations = Collections.unmodifiableList(annotations);
		METHOD_PARAM_ANNOTATION_CACHE.put(method, annotations);
		return annotations;
	}

	/**
	 * 
	 * @param <T>
	 * @param method
	 * @param type
	 * @return
	 */
	public static <T extends Annotation>
		List<List<T>> getParameterAnnotations(Method method, Class<T> type) {
		List<List<T>> annotations = new ArrayList<List<T>>();
		for (List<Annotation> paramAnnotations : getParameterAnnotations(method)) {
			List<T> listAnnotations = new ArrayList<T>();
			for (Annotation a : paramAnnotations) {
				if (type.isInstance(a)) {
					listAnnotations.add(type.cast(a));
				}
			}
			annotations.add(listAnnotations);
		}
		return annotations;
	}

}
