package org.apache.thrift.cache;

public class DependentCacheFunctionConfigurationBuilder {

	private FunctionCacheConfigurationBuilder functionCacheConfigurationBuilder;
	private DependentCacheFunctionConfiguration dependentCacheFunctionConfiguration;

	public DependentCacheFunctionConfigurationBuilder(
			FunctionCacheConfigurationBuilder functionCacheConfigurationBuilder, String dependentFunctionName) {
		this.functionCacheConfigurationBuilder = functionCacheConfigurationBuilder;
		this.dependentCacheFunctionConfiguration = new DependentCacheFunctionConfiguration(dependentFunctionName);
	}

	public DependentCacheFunctionConfigurationBuilder addFieldMapping(String functionField, String dependentField) {
		this.dependentCacheFunctionConfiguration.addFieldMapping(functionField, dependentField);
		return this;
	}

	public DependentCacheFunctionConfigurationBuilder partialKey() {
		this.dependentCacheFunctionConfiguration.setPartialKey(true);
		return this;
	}

	public FunctionCacheConfigurationBuilder build() {
		this.functionCacheConfigurationBuilder.getFunctionConfiguration()
				.addDependentFunction(this.dependentCacheFunctionConfiguration);
		return this.functionCacheConfigurationBuilder;
	}

}
