package org.apache.thrift.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CacheConfiguration {

	private Map<String, FunctionCacheConfiguration> functionConfigurations = new HashMap<>();

	public CacheConfiguration() {
		// TODO Auto-generated constructor stub
	}

	public void addFunctionConfiguration(FunctionCacheConfiguration functionConfiguration) {
		this.functionConfigurations.put(functionConfiguration.getFunctionName(), functionConfiguration);
	}
	
	public Optional<FunctionCacheConfiguration> getFunctionCacheConfiguration(String functionName) {
		return Optional.ofNullable(functionConfigurations.get(functionName));
	}

}
