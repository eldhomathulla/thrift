package org.apache.thrift.cache;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TBase;

public abstract class DefaultCache implements TCache {
	protected CacheConfiguration cacheConfiguration;

	public DefaultCache(CacheConfiguration cacheConfiguration) {
		this.cacheConfiguration = cacheConfiguration;
	}

	@Override
	public void write(TCacheKey key, TBase value) {
		// TODO Auto-generated method stub

	}

	@Override
	public TBase read(TCacheKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(TCacheKey key) {
		// TODO Auto-generated method stub
	}

	@Override
	public List<TBase> readFromPartialKey(TCacheKey partialKey) throws TCacheFunctionNotImplemented {
		throw new TCacheFunctionNotImplemented("readPartial Function Not Implemented");
	}

	@Override
	public void delete(TCacheKey key, boolean partial) {
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<TCacheKey, TBase> readAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postProcess(TCacheKey tCacheKey) {
		FunctionCacheConfiguration functionCacheConfiguration = this.cacheConfiguration
				.getFunctionCacheConfiguration(tCacheKey.getFunctionName()).orElseGet(() -> null);
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

}
