package org.apache.thrift.cache;

import java.util.Map;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

public interface TCache {

	public void write(TCacheKey key, @SuppressWarnings("rawtypes") TBase value) throws TException;

	@SuppressWarnings("rawtypes")
	public TBase read(TCacheKey key) throws TException;

	public void delete(TCacheKey key) throws TException;

	public void delete(TCacheKey key, boolean partial) throws TException;
	
	public void deleteAll() throws TException;

	@SuppressWarnings("rawtypes")
	public Map<TCacheKey, TBase> readAll() throws TException;

	@SuppressWarnings("rawtypes")
	public Map<TCacheKey, TBase> readFromPartialKey(TCacheKey partialKey) throws TException;
	
	public void postProcess(TCacheKey tCacheKey) throws TException; 

}
