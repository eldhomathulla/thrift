package org.apache.thrift.cache;

public class CacheConfigurationBuilder {

	private CacheConfiguration cacheConfiguration = new CacheConfiguration();

	public FunctionCacheConfigurationBuilder createFunctionConfiguration(String functionName) {
		return new FunctionCacheConfigurationBuilder(this,functionName);
	}
	
	public CacheConfigurationBuilder addConfiguration(String configName,Object configValue) {
		cacheConfiguration.addConfiguration(configName, configValue);
		return this;
	}

	public CacheConfiguration getCacheConfiguration() {
		return cacheConfiguration;
	}
	
	public CacheConfiguration build() {
		return cacheConfiguration;
	}

}
