package org.apache.thrift.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class ThriftLockFactory {

	private Map<String, ReadWriteLock> locks = new HashMap<>();
	private Function<String, ReadWriteLock> createLock = (String lockName) -> new ReentrantReadWriteLock();

	public ThriftLockFactory(Function<String, ReadWriteLock> createLock) {
		this.createLock = createLock;
	}

	public ThriftLockFactory() {
	}

	public ReadWriteLock getLock(String lockName) {
		if (locks.containsKey(lockName)) {
			return locks.get(lockName);
		} else {
			synchronized (locks) {
				ReadWriteLock lock = createLock.apply(lockName);
				locks.put(lockName, lock);
				return lock;
			}
		}
	}

}
