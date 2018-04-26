package org.apache.thrift.cache;

import java.util.Map;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

public interface TCache {

	public void write(TCacheKey key, @SuppressWarnings("rawtypes") TBase value) throws TException;

	/**
	 * reading from the  cache based on tcache key
	 * @param key
	 * @return
	 * @throws TException
	 */
	@SuppressWarnings("rawtypes")
	public TBase read(TCacheKey key) throws TException;

	/**
	 * Deleting from the cache based on the tcache key
	 * @param key
	 * @throws TException
	 */
	public void delete(TCacheKey key) throws TException;

	/**
	 * Deleting from the cache based on tcache key and if the deletion should be done f tcache key is partial.
	 * @param key
	 * @param partial
	 * @throws TException
	 */
	public void delete(TCacheKey key, boolean partial) throws TException;
	
	/**
	 * Delete all entries in the cache
	 * @throws TException
	 */
	public void deleteAll() throws TException;

	/**
	 * Read al entries from the cache
	 * @return
	 * @throws TException
	 */
	@SuppressWarnings("rawtypes")
	public Map<TCacheKey, TBase> readAll() throws TException;

	/**
	 * Read all entries that matches the partial key
	 * @param partialKey
	 * @return
	 * @throws TException
	 */
	@SuppressWarnings("rawtypes")
	public Map<TCacheKey, TBase> readFromPartialKey(TCacheKey partialKey) throws TException;
	
	/**
	 * Handles any miscellaneous operation other than the other specified cache operations
	 * @param tCacheKey
	 * @param iFaceClassName
	 * @param processFunctionClassName
	 * @param argsClassName
	 * @throws TException
	 */
	public void postProcess(TCacheKey tCacheKey,String iFaceClassName,String processFunctionClassName,String argsClassName) throws TException; 

}
