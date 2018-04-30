package org.apache.thrift.cache;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map.Entry;

public class CacheConfigurationBuilder {

	private CacheConfiguration cacheConfiguration = new CacheConfiguration();

	public FunctionCacheConfigurationBuilder createFunctionConfiguration(String functionName) {
		return new FunctionCacheConfigurationBuilder(this, functionName);
	}

	public CacheConfigurationBuilder addConfiguration(String configName, Object configValue) {
		cacheConfiguration.addConfiguration(configName, configValue);
		return this;
	}

	public CacheConfiguration getCacheConfiguration() {
		return cacheConfiguration;
	}

	public CacheConfigurationBuilder enableCacheAll() {
		cacheConfiguration.setCacheAll(true);
		return this;
	}

	public CacheConfigurationBuilder disableCacheAll() {
		cacheConfiguration.setCacheAll(false);
		return this;
	}

	public CacheConfigurationBuilder loadConfiguration(Properties props) {
		props.stringPropertyNames().stream().forEach((String key) -> {
			cacheConfiguration.addConfiguration(key, props.getProperty(key));
		});
		return this;
	}

	public CacheConfiguration build() {
		Set<String> dependentFunctions = new HashSet<>();
		cacheConfiguration.getFunctionConfigurations().entrySet().stream()
				.forEach((Entry<String, FunctionCacheConfiguration> entry) -> {
					dependentFunctions.addAll(entry.getValue().getDependentCacheFunctionConfiguration().stream()
							.map(DependentCacheFunctionConfiguration::getFunctionName).collect(Collectors.toSet()));
				});
		dependentFunctions.parallelStream().forEach(
				(String functionName) -> cacheConfiguration.getFunctionCacheConfiguration(functionName).ifPresent(
						(FunctionCacheConfiguration fununctionConfig) -> fununctionConfig.setReCalculate(true)));
		return cacheConfiguration;
	}

}
