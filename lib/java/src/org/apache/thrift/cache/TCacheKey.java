package org.apache.thrift.cache;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TBase;

/**
 * 
 * Used as key for the TCache. Contains all parameters and it values for the corresponding functions.
 *
 */
public class TCacheKey {

	private Map<String, Object> args = new HashMap<String, Object>();
	public static final String FUNCTION_NAME = "function_name";

	public TCacheKey(String functionName) {
		this.addArg(FUNCTION_NAME, functionName);
	}

	public TCacheKey() {

	}

	public TCacheKey(TCacheKey tCacheKey) {
		this.args = tCacheKey.getArgs();
	}

	public TCacheKey(TBase argsTbase) {
		Field[] argsFields = argsTbase.getClass().getFields();
		args.put(FUNCTION_NAME, argsTbase.getClass().getSimpleName().replaceFirst("_args$", ""));
		Arrays.stream(argsFields).forEach((Field field) -> {
			try {
				if (!field.getName().equals("metaDataMap")) {
					this.getArgs().put(field.getName(), field.get(argsTbase));
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {

			}
		});
	}

	public void addArg(String arg, Object argValue) {
		this.getArgs().put(arg, argValue);
	}

	public String functionName() {
		return (String) args.get(FUNCTION_NAME);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TCacheKey) {
			return this.getArgs().equals(((TCacheKey) obj).getArgs());
		}
		return super.equals(obj);
	}

	public Map<String, Object> getArgs() {
		return args;
	}

	public void setArgs(Map<String, Object> args) {
		this.args = args;
	}

	public Map<String, Object> fields() {
		Map<String, Object> fields = new HashMap<>(args);
		fields.remove(FUNCTION_NAME);
		return fields;
	}

	@Override
	public int hashCode() {
		return args.hashCode();
	}

	@Override
	public String toString() {
		return "TCacheKey: \n" + args.toString();
	}
}
