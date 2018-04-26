package org.apache.thrift.cache;

public class DependentFunctionActionHolder {

	private TCacheKey tCacheKey;
	private String iFaceClassName;
	private String processFunctionClassName;
	private String argsClassName;

	public DependentFunctionActionHolder(TCacheKey tCacheKey, String iFaceClassName,String processFunctionClassName,String argsClassName) {
		this.settCacheKey(tCacheKey);
		this.setiFaceClassName(iFaceClassName);
		this.setProcessFunctionClassName(processFunctionClassName);
		this.setArgsClassName(argsClassName);
	}

	public TCacheKey gettCacheKey() {
		return tCacheKey;
	}

	public void settCacheKey(TCacheKey tCacheKey) {
		this.tCacheKey = tCacheKey;
	}

	public String getiFaceClassName() {
		return iFaceClassName;
	}

	public void setiFaceClassName(String iFaceClassName) {
		this.iFaceClassName = iFaceClassName;
	}

	public String getProcessFunctionClassName() {
		return processFunctionClassName;
	}

	public void setProcessFunctionClassName(String processFunctionClassName) {
		this.processFunctionClassName = processFunctionClassName;
	}

	public String getArgsClassName() {
		return argsClassName;
	}

	public void setArgsClassName(String argsClassName) {
		this.argsClassName = argsClassName;
	}

}
