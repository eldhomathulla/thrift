package org.apache.thrift;

import java.util.Map;

public interface TCache {

	public void write(TCacheKey key, TBase value);

	public TBase read(TCacheKey key);

	public void delete(TCacheKey key);

	public void deleteAll();

	public Map<TCacheKey, TBase> readAll();

}
