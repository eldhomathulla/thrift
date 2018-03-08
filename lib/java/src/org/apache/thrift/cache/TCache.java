package org.apache.thrift.cache;

import java.util.List;
import java.util.Map;

import org.apache.thrift.TBase;

public interface TCache {

	public void write(TCacheKey key, TBase value);

	public TBase read(TCacheKey key);

	public void delete(TCacheKey key);

	public void delete(TCacheKey key, boolean partial) throws TCacheFunctionNotImplemented;
	
	public void deleteAll();

	public Map<TCacheKey, TBase> readAll();

	public List<TBase> readFromPartialKey(TCacheKey partialKey) throws TCacheFunctionNotImplemented;
	
	public void postProcess(TCacheKey tCacheKey); 

}
