package org.apache.thrift.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class ThriftLockFactory {

	private Map<String, Lock> locks = new HashMap<>();
	private Function<String, Lock> createLock = (String lockName) -> new ReentrantLock();

	public ThriftLockFactory(Function<String, Lock> createLock) {
		this.createLock = createLock;
	}

	public ThriftLockFactory() {
	}

	public Lock getLock(String lockName) {
		if (locks.containsKey(lockName)) {
			return locks.get(lockName);
		} else {
			synchronized (locks) {
				Lock lock = createLock.apply(lockName);
				locks.put(lockName, lock);
				return lock;
			}
		}
	}

}
