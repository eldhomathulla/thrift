package org.apache.thrift.cache;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TBase;

public class TCacheKey {

	private Map<String, Object> args = new HashMap<String, Object>();
	private static final String FUNCTION_NAME = "function_name";

	public TCacheKey(String functionName) {
		this.addArg(FUNCTION_NAME, functionName);
	}
	
	public TCacheKey() {
		
	}
	
	public TCacheKey(TCacheKey tCacheKey) {
		this.args=tCacheKey.getArgs();
	}

	public TCacheKey(TBase argsTbase) {
		Field[] argsFields = argsTbase.getClass().getFields();
		args.put(FUNCTION_NAME, argsTbase.getClass().getSimpleName().replaceFirst("_args$", ""));
		Arrays.stream(argsFields).forEach((Field field) -> {
			try {
				this.getArgs().put(field.getName(), field.get(argsTbase));
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
	
	public  void setArgs(Map<String, Object> args) {
		this.args=args;
	}

	@Override
	public int hashCode() {
		return args.hashCode();
	}

	@Override
	public String toString() {
		return "TCacheKey: \n"+args.toString();
	}
}
