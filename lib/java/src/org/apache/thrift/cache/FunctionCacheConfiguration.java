package org.apache.thrift.cache;

import java.util.ArrayList;
import java.util.List;

public class FunctionCacheConfiguration {

	private String functionName;
	private List<DependentCacheFunctionConfiguration> dependentFunctions = new ArrayList<>();
	private boolean reCalculate=false;

	public FunctionCacheConfiguration(String functionName) {
		this.setFunctionName(functionName);
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public void addDependentFunction(DependentCacheFunctionConfiguration dependentCacheFunctionConfiguration) {
		this.dependentFunctions.add(dependentCacheFunctionConfiguration);
	}
	
	public List<DependentCacheFunctionConfiguration> getDependentCacheFunctionConfiguration(){
		return this.dependentFunctions;
	}

	public boolean isReCalculate() {
		return reCalculate;
	}

	public void setReCalculate(boolean reCalculate) {
		this.reCalculate = reCalculate;
	}

}
