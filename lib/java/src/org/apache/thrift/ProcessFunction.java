package org.apache.thrift;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.thrift.cache.TCache;
import org.apache.thrift.cache.TCacheKey;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProcessFunction<I, T extends TBase> {
	private final String methodName;
	private static Optional<TCache> cache = Optional.empty();

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessFunction.class.getName());

	public ProcessFunction(String methodName) {
		this.methodName = methodName;
	}

	public final void process(int seqid, TProtocol iprot, TProtocol oprot, I iface) throws TException {
		T args = getEmptyArgsInstance();
		try {
			args.read(iprot);
		} catch (TProtocolException e) {
			iprot.readMessageEnd();
			TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
			oprot.writeMessageBegin(new TMessage(getMethodName(), TMessageType.EXCEPTION, seqid));
			x.write(oprot);
			oprot.writeMessageEnd();
			oprot.getTransport().flush();
			return;
		}
		iprot.readMessageEnd();
		@SuppressWarnings("rawtypes")
		TBase result = null;
		try {
			result = cache.map((TCache tCache) -> {
				try {
					return tCache.read(new TCacheKey(args));
				} catch (TException e1) {
					throw new RuntimeException(e1);
				}
			}).orElseGet(() -> {
				try {
					return processResult(args, iface, oprot, seqid);
				} catch (TException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (NoSuchElementException ne) {
			return;
		}
		if (!isOneway()) {
			oprot.writeMessageBegin(new TMessage(getMethodName(), TMessageType.REPLY, seqid));
			result.write(oprot);
			oprot.writeMessageEnd();
			oprot.getTransport().flush();
		}
	}

	public TBase processResult(T args, I iface, TProtocol oprot, int seqid) throws TException {
		try {
			TBase result = getResult(iface, args);
			if (!isEmpty(result)) {
				cache.ifPresent((TCache tCache) -> {
					try {
						tCache.write(new TCacheKey(args), result);
					} catch (TException e) {
						throw new RuntimeException(e);
					}
				});
			}
			cache.ifPresent((TCache tCache)->{
				try {
					tCache.postProcess(new TCacheKey(args));
				} catch (TException e) {
					throw new RuntimeException(e);
				}
			});
			return result;
		} catch (TException tex) {
			LOGGER.error("Internal error processing " + getMethodName(), tex);
			handleException(seqid, oprot);
			return null;
		} catch (RuntimeException rex) {
			LOGGER.error("Internal error processing " + getMethodName(), rex);
			if (handleRuntimeExceptions()) {
				handleException(seqid, oprot);
			}
			return null;
		}
	}

	public boolean isEmpty(TBase tbase) {
		Field[] tbaseFields = tbase.getClass().getFields();
		if (tbaseFields.length == 1 && "metaDataMap" == tbaseFields[0].getName()) {
			return true;
		} else {
			return false;
		}
	}

	private void handleException(int seqid, TProtocol oprot) throws TException {
		if (!isOneway()) {
			TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR,
					"Internal error processing " + getMethodName());
			oprot.writeMessageBegin(new TMessage(getMethodName(), TMessageType.EXCEPTION, seqid));
			x.write(oprot);
			oprot.writeMessageEnd();
			oprot.getTransport().flush();
		}
	}

	protected boolean handleRuntimeExceptions() {
		return false;
	}

	protected abstract boolean isOneway();

	public abstract TBase getResult(I iface, T args) throws TException;

	public abstract T getEmptyArgsInstance();

	public String getMethodName() {
		return methodName;
	}

	public static Optional<TCache> getCache() {
		return cache;
	}

	public static void setCache(Optional<TCache> cache) {
		ProcessFunction.cache = cache;
	}
}
