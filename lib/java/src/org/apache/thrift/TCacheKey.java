package org.apache.thrift;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TCacheKey {

	private Map<String, Object> args = new HashMap<String, Object>();

	public TCacheKey() {

	}

	public TCacheKey(TBase argsTbase) {
		Field[] argsFields = argsTbase.getClass().getFields();
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

	@Override
	public int hashCode() {
		return args.hashCode();
	}
}
