package org.apache.thrift.cache;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

public abstract class DefaultCache implements TCache {
	protected static final Logger LOGGER = Logger.getLogger(DefaultCache.class.getCanonicalName());

	protected CacheConfiguration cacheConfiguration;
	private ExecutorService executorService;
	private Map<String, List<DependentFunctionActionHolder>> dependentFunctionExecutions = new ConcurrentHashMap<>();
	private Function<String, List<DependentFunctionActionHolder>> dependentFunctionActionHolderListSupplier = (
			String str) -> Collections.synchronizedList(new LinkedList<>());
	private Map<String, Object> ifaces = new HashMap<>();
	private ThriftLockFactory thriftLockFactory = new ThriftLockFactory();
	protected int nThreads = 5;

	public DefaultCache(CacheConfiguration cacheConfiguration) {
		this.cacheConfiguration = cacheConfiguration;
		executorService = Executors.newFixedThreadPool(nThreads);
		cacheConfiguration.getFunctionConfigurations().values().stream()
				.forEach((FunctionCacheConfiguration functionConfig) -> {
					if (functionConfig.isReCalculate()) {
						dependentFunctionExecutions.put(functionConfig.getFunctionName(),
								dependentFunctionActionHolderListSupplier.apply(functionConfig.getFunctionName()));
					}
				});
	}

	public DefaultCache(CacheConfiguration cacheConfiguration, Object... ifaces) {
		this(cacheConfiguration);
		Arrays.stream(ifaces).forEach((Object iface) -> addIface(iface));
	}

	public DefaultCache(CacheConfiguration cacheConfiguration, ThriftLockFactory thriftLockFactory, Object... ifaces) {
		this(cacheConfiguration);
		Arrays.stream(ifaces).forEach((Object iface) -> addIface(iface));
		this.thriftLockFactory = thriftLockFactory;
	}

	@Override
	public void write(TCacheKey key, TBase value) throws TException {
		if (cacheConfiguration.isCacheAll() || cacheConfiguration.shouldCacheFunction(key.functionName())) {
			LOGGER.info("Writing to cache " + key);
			this.writeToCache(key, value);
		}
	}

	@Override
	public TBase read(TCacheKey key) throws TException {
		LOGGER.info("reading from cache " + key);
		if (cacheConfiguration.isCacheAll() || cacheConfiguration.shouldCacheFunction(key.functionName())) {
			return readFromCache(key);
		}
		LOGGER.info("Key " + key + " not found in cache");
		return null;
	}

	@Override
	public abstract void delete(TCacheKey key) throws TException;

	@Override
	public void delete(TCacheKey key, boolean partial) throws TCacheFunctionNotImplementedException, TException {
		if (!partial) {
			delete(key);
		}
	}

	@Override
	public void deleteAll() throws TException {
		throw new TCacheFunctionNotImplementedException("deleteAll function has not been implemented");

	}

	@Override
	public Map<TCacheKey, TBase> readAll() throws TException {
		throw new TCacheFunctionNotImplementedException("readAll function has not been implemented");
	}

	@Override
	public Map<TCacheKey, TBase> readFromPartialKey(TCacheKey partialKey)
			throws TCacheFunctionNotImplementedException, TException {
		throw new TCacheFunctionNotImplementedException("readFromPartialKey function has not been implemented");
	}

	@Override
	public void postProcess(TCacheKey tCacheKey, String iFaceClassName, String processFunctionClassName,
			String argsClassName) throws TException {
		LOGGER.info("Post processing " + tCacheKey);
		FunctionCacheConfiguration functionCacheConfiguration = this.cacheConfiguration
				.getFunctionCacheConfiguration(tCacheKey.functionName()).orElseGet(() -> null);
		if (functionCacheConfiguration != null) {

			// deleting all cache entries for dependent function and re populating it
			for (DependentCacheFunctionConfiguration dependentFunction : functionCacheConfiguration
					.getDependentCacheFunctionConfiguration()) {
				Runnable job = createPostProcessAction(tCacheKey, dependentFunction);
				executorService.execute(job);
			}
			// storing of necessary information for recalculating the cache entries for the
			// function when needed later
			if (functionCacheConfiguration.isReCalculate()) {
				List<DependentFunctionActionHolder> functionExecutions;
				if (dependentFunctionExecutions.containsKey(functionCacheConfiguration.getFunctionName())) {
					functionExecutions = dependentFunctionExecutions.get(functionCacheConfiguration.getFunctionName());
				} else {
					functionExecutions = dependentFunctionActionHolderListSupplier
							.apply(functionCacheConfiguration.getFunctionName());
					dependentFunctionExecutions.put(functionCacheConfiguration.getFunctionName(), functionExecutions);
				}
				functionExecutions.add(new DependentFunctionActionHolder(tCacheKey, iFaceClassName,
						processFunctionClassName, argsClassName));
			}
		}
		LOGGER.info("Post processing Completed " + tCacheKey);
	}

	/**
	 * Creating runnable instance containing the post process actions like deletion
	 * of dependent cache entries and corresponding cache re population
	 * 
	 * @param functionCacheKey
	 * @param dependentFunction
	 * @return
	 */
	private Runnable createPostProcessAction(TCacheKey functionCacheKey,
			DependentCacheFunctionConfiguration dependentFunction) {
		Runnable job = () -> {
			TCacheKey dependentFunctionCacheKey;
			Map<String, String> fieldMappings = dependentFunction.getFieldMappings();

			// Mapping the field values between the called function and corresponding
			// dependent function
			if (!fieldMappings.isEmpty()) {
				dependentFunctionCacheKey = new TCacheKey(dependentFunction.getFunctionName());
				for (Entry<String, String> fieldMapping : fieldMappings.entrySet()) {
					Object value = extractValue(functionCacheKey, fieldMapping);
					dependentFunctionCacheKey.addArg(fieldMapping.getValue(), value);
				}
			} else {
				dependentFunctionCacheKey = new TCacheKey(functionCacheKey);
			}
			dependentFunctionCacheKey.addArg(TCacheKey.FUNCTION_NAME, dependentFunction.getFunctionName());
			try {
				delete(dependentFunctionCacheKey, dependentFunction.isPartialKey());
				LOGGER.info("Deleting entry " + dependentFunctionCacheKey);

				// re populating the cache entries for the dependent function after it has been
				// deleted
				if (this.dependentFunctionExecutions.containsKey(dependentFunction.getFunctionName())) {
					this.dependentFunctionExecutions.get(dependentFunction.getFunctionName()).parallelStream()
							.forEach((DependentFunctionActionHolder dependentFunctionActionHalder) -> {
								rePopulateCache(functionCacheKey, fieldMappings, dependentFunctionActionHalder);
							});
				}
			} catch (TCacheFunctionNotImplementedException e) {
				LOGGER.log(Level.WARNING, "Deletion function has not been implemented", e);
			} catch (TException e) {
				LOGGER.log(Level.SEVERE,
						"Deletion of cache failed for the dependent function :" + dependentFunction.getFunctionName(),
						e);
			}
		};
		return job;
	}

	/**
	 * Function called for re populating the cache when the function entries are
	 * deleted
	 * 
	 * @param functionCacheKey
	 * @param fieldMappings
	 * @param dependentFunctionActionHalder
	 */
	private void rePopulateCache(TCacheKey functionCacheKey, Map<String, String> fieldMappings,
			DependentFunctionActionHolder dependentFunctionActionHalder) {
		if (!ifaces.containsKey(dependentFunctionActionHalder.getiFaceClassName())) {
			return;
		}

		// Re populate cache for the dependent function call if value for the specified
		// field in the field mapping is equal for the called function and dependent
		// function
		boolean rePolulate = true;
		if (!fieldMappings.isEmpty()) {
			TCacheKey dependentFunctionCacheKey = dependentFunctionActionHalder.gettCacheKey();
			for (Entry<String, String> fieldMapping : fieldMappings.entrySet()) {
				Object value = extractValue(functionCacheKey, fieldMapping);
				if (value != null && value.equals(dependentFunctionCacheKey.getArgs().get(fieldMapping.getValue()))) {
					rePolulate = rePolulate && true;
				} else {
					rePolulate = false;
					break;
				}
			}
		}

		if (rePolulate) {
			executorService.execute(() -> {
				try {
					Lock lock = thriftLockFactory.getLock(dependentFunctionActionHalder.gettCacheKey().toString());
					lock.lock();
					if (read(dependentFunctionActionHalder.gettCacheKey()) == null) {
						TBase result = getResult(dependentFunctionActionHalder);
						LOGGER.info("repopulating cache :" + dependentFunctionActionHalder.gettCacheKey());
						write(dependentFunctionActionHalder.gettCacheKey(), result);
					}
					lock.unlock();
				} catch (TException e) {
					throw new TCacheException(e);
				}

			});
		}
	}

	/**
	 * Re calculating the result for the dependent function
	 * 
	 * @param dependentFunctionActionHalder
	 * @return
	 */
	private TBase getResult(DependentFunctionActionHolder dependentFunctionActionHalder) {
		try {
			TCacheKey tCacheKey = dependentFunctionActionHalder.gettCacheKey();
			Class<?> argsClass = Class.forName(dependentFunctionActionHalder.getArgsClassName());

			// Creating args object for calling thrift function
			Constructor<?> constructor = argsClass.getConstructor();
			constructor.setAccessible(true);
			Object args = constructor.newInstance();
			Map<String, Object> fields = tCacheKey.fields();
			Field[] argsFields = argsClass.getFields();
			for (Field argsField : argsFields) {
				if (!argsField.getName().equals("metaDataMap")) {
					argsField.set(args, fields.get(argsField.getName()));
				}
			}
			Object iface = ifaces.get(dependentFunctionActionHalder.getiFaceClassName());
			Class<?> processFunctionClass = Class.forName(dependentFunctionActionHalder.getProcessFunctionClassName());
			Object thriftFunction = processFunctionClass.newInstance();

			// Searching for getResultfunction which is used for invoking the thrift
			// function and getting the result
			for (Method processFunctionMethod : processFunctionClass.getMethods()) {
				if ("getResult".equals(processFunctionMethod.getName())) {
					return (TBase) processFunctionMethod.invoke(thriftFunction, iface, args);
				}
			}
			return null;
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException
				| NoSuchMethodException e) {
			throw new TCacheException(e);
		}
	}

	/**
	 * Creating value object from field mapping and the cache key of the mapped
	 * function
	 * 
	 * @param tCacheKey
	 * @param fieldMapping
	 * @return
	 */
	private Object extractValue(TCacheKey tCacheKey, Entry<String, String> fieldMapping) {
		Object value;
		String key = fieldMapping.getKey();
		if (key.contains(".")) {

			// getting value when the mapping key for the executed function is for a nested
			// value. For example: value.value1.value2 : valueDependent

			String[] fields = key.split("\\.");
			value = tCacheKey.getArgs().get(fields[0]);
			for (int i = 1; i < fields.length; i++) {
				String field = fields[i];
				Class<?> valueClass = value.getClass();
				Field[] declaredFields = valueClass.getDeclaredFields();
				for (Field declaredField : declaredFields) {
					if (declaredField.getName().equals(field)) {
						declaredField.setAccessible(true);
						try {
							value = declaredField.get(value);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new TCacheException(e);
						}
					}
				}
				;
			}
		} else {
			// Extracting value for the straight forward mapping
			value = tCacheKey.getArgs().get(key);
		}
		return value;
	}

	protected abstract void writeToCache(TCacheKey key, TBase value) throws TException;

	protected abstract TBase readFromCache(TCacheKey key) throws TException;

	public Function<String, List<DependentFunctionActionHolder>> getDependentFunctionActionHolderListSupplier() {
		return dependentFunctionActionHolderListSupplier;
	}

	public void setDependentFunctionActionHolderListSupplier(
			Function<String, List<DependentFunctionActionHolder>> dependentFunctionActionHolderListSupplier) {
		this.dependentFunctionActionHolderListSupplier = dependentFunctionActionHolderListSupplier;
	}

	public Map<String, Object> getIfaces() {
		return ifaces;
	}

	public void setIfaces(Map<String, Object> ifaces) {
		this.ifaces = ifaces;
	}

	public DefaultCache addIface(Object iface) {
		this.getIfaces().put(iface.getClass().getName(), iface);
		return this;
	}

}
