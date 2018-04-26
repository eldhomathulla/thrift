package org.apache.thrift.cache;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestTCache {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPostProcessBasic() throws TException {
		CacheConfigurationBuilder cacheConfigurationBuilder=new CacheConfigurationBuilder();
		CacheConfiguration cacheConfiguration=cacheConfigurationBuilder.createFunctionConfiguration("test1").createDependentFunction("test2").addFieldMapping("id", "testId").build().build().build();
		TCache tCache=new TestCache(cacheConfiguration);
		TCacheKey tCacheKey=new TCacheKey("test1");
		tCacheKey.addArg("id", "first");
		tCache.postProcess(tCacheKey, null, null, null);
		List<TCacheKey> expected=new ArrayList<>(1);
		TCacheKey tCacheKey2=new TCacheKey("test2");
		tCacheKey2.addArg("testId", "first");
		expected.add(tCacheKey2);
		assertEquals(expected, ((TestCache) tCache).getDeleteCalls());
	}
	
	@Test
	public void testPostProcessNoDependentFunctions() throws TException {
		CacheConfigurationBuilder cacheConfigurationBuilder=new CacheConfigurationBuilder();
		CacheConfiguration cacheConfiguration=cacheConfigurationBuilder.createFunctionConfiguration("test1").build().build();
		TCacheKey tCacheKey=new TCacheKey("test1");
		tCacheKey.addArg("id", "first");
		TCache tCache=new TestCache(cacheConfiguration);
		tCache.postProcess(tCacheKey, null, null, null);
		assertTrue(((TestCache) tCache).getDeleteCalls().isEmpty());
	}
	
	
	static class TestCache extends DefaultCache{
		private List<TCacheKey> deleteCalls=new LinkedList<>();
		

		public TestCache(CacheConfiguration cacheConfiguration) {
			super(cacheConfiguration);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public void delete(TCacheKey key, boolean partial) throws TCacheFunctionNotImplementedException, TException {
			deleteCalls.add(key);
			super.delete(key, partial);
		}
		
		
		public List<TCacheKey> getDeleteCalls(){
			return this.deleteCalls;
		}

		@Override
		public void delete(TCacheKey key) throws TException {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void writeToCache(TCacheKey key, TBase value) throws TException {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected TBase readFromCache(TCacheKey key) throws TException {
			// TODO Auto-generated method stub
			return null;
		}
		
		
	}
	 

}
