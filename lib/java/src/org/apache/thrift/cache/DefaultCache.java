package org.apache.thrift.cache;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

public abstract class DefaultCache implements TCache {
	protected CacheConfiguration cacheConfiguration;

	public DefaultCache(CacheConfiguration cacheConfiguration) {
		this.cacheConfiguration = cacheConfiguration;
	}

	@Override
	public void write(TCacheKey key, TBase value) throws TException {
		if(cacheConfiguration.isCacheAll() || cacheConfiguration.shouldCacheFunction(key.functionName())) {
			this.writeToCache(key, value);
		}
	}

	@Override
	public TBase read(TCacheKey key) throws TException {
		if(cacheConfiguration.isCacheAll() || cacheConfiguration.shouldCacheFunction(key.functionName())) {
			return readFromCache(key);
		}
		return null;
	}

	@Override
	public void delete(TCacheKey key) throws TException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(TCacheKey key, boolean partial) throws TCacheFunctionNotImplementedException, TException {
		if (!partial) {
			delete(key);
		}
	}

	@Override
	public void deleteAll() throws TException {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<TCacheKey, TBase> readAll() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<TCacheKey, TBase> readFromPartialKey(TCacheKey partialKey)
			throws TCacheFunctionNotImplementedException, TException {
		throw new TCacheFunctionNotImplementedException("readFromPartialKey function has not been implemented");
	}

	@Override
	public void postProcess(TCacheKey tCacheKey) throws TException {
		FunctionCacheConfiguration functionCacheConfiguration = this.cacheConfiguration
				.getFunctionCacheConfiguration(tCacheKey.functionName()).orElseGet(() -> null);
		if (functionCacheConfiguration != null) {
			for (DependentCacheFunctionConfiguration dependentFunction : functionCacheConfiguration
					.getDependentCacheFunctionConfiguration()) {
				TCacheKey tCacheKey2;
				Map<String, String> fieldMappings = dependentFunction.getFieldMappings();
				if (!fieldMappings.isEmpty()) {
					tCacheKey2 = new TCacheKey(dependentFunction.getFunctionName());
					for (Entry<String, String> fieldMapping : fieldMappings.entrySet()) {
						tCacheKey2.addArg(fieldMapping.getValue(), tCacheKey.getArgs().get(fieldMapping.getKey()));
					}
				} else {
					tCacheKey2 = new TCacheKey(tCacheKey);
				}
				delete(tCacheKey2, dependentFunction.isPartialKey());
			}
		}
	}
	
	protected void writeToCache(TCacheKey key, TBase value) throws TException {
		
	}
	
	protected TBase readFromCache(TCacheKey key) throws TException {
		return null;
	}
	
}
