package org.apache.thrift.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CacheConfiguration {

	private Map<String, FunctionCacheConfiguration> functionConfigurations = new HashMap<>();
	private Map<String,Object> configurations=new HashMap<>();
	private boolean cacheAll=true;
			

	public CacheConfiguration() {
		// TODO Auto-generated constructor stub
	}

	public CacheConfiguration addFunctionConfiguration(FunctionCacheConfiguration functionConfiguration) {
		this.functionConfigurations.put(functionConfiguration.getFunctionName(), functionConfiguration);
		return this;
	}
	
	public CacheConfiguration  addConfiguration(String configName,Object configValue) {
		this.getConfigurations().put(configName, configValue);
		return this;
	}
	
	public Optional<FunctionCacheConfiguration> getFunctionCacheConfiguration(String functionName) {
		return Optional.ofNullable(functionConfigurations.get(functionName));
	}

	public Map<String,Object> getConfigurations() {
		return configurations;
	}
	
	public Object getConfiguration(String configName) {
		return this.configurations.get(configName);
	}
	
	public CacheConfiguration enableCacheAll() {
		this.setCacheAll(true);
		return this;
	}
	
	public CacheConfiguration disableCacheAll() {
		this.setCacheAll(false);
		return this;
	}

	public boolean isCacheAll() {
		return cacheAll;
	}

	public void setCacheAll(boolean cacheAll) {
		this.cacheAll = cacheAll;
	}

	public boolean shouldCacheFunction(String functionName) {
		return this.functionConfigurations.containsKey(functionName);
	}

}
