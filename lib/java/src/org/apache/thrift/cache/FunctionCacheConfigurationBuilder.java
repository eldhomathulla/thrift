package org.apache.thrift.cache;

public class FunctionCacheConfigurationBuilder {

	private CacheConfigurationBuilder cacheConfigurationBuilder;
	private FunctionCacheConfiguration functionConfiguration;

	public FunctionCacheConfigurationBuilder(CacheConfigurationBuilder cacheConfigurationBuilder, String functionName) {
		this.cacheConfigurationBuilder = cacheConfigurationBuilder;
		functionConfiguration = new FunctionCacheConfiguration(functionName);
	}

	public DependentCacheFunctionConfigurationBuilder createDependentFunction(String functionName) {
		return new DependentCacheFunctionConfigurationBuilder(this, functionName);
	}

	public CacheConfigurationBuilder build() {
		this.cacheConfigurationBuilder.getCacheConfiguration().addFunctionConfiguration(functionConfiguration);
		return this.cacheConfigurationBuilder;
	}

	public FunctionCacheConfiguration getFunctionConfiguration() {
		return this.functionConfiguration;
	}

}
