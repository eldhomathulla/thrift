package org.apache.thrift.cache;

import java.util.HashMap;
import java.util.Map;

public class DependentCacheFunctionConfiguration {

	private String functionName;
	private Map<String, String> fieldMappings=new HashMap<>();
	private boolean partialKey=false;

	public DependentCacheFunctionConfiguration(String functionName) {
		this.setFunctionName(functionName);
	}
	
	public void addFieldMapping(String functionFieldName,String dependentFieldName) {
		this.fieldMappings.put(functionFieldName, dependentFieldName);
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public Map<String, String> getFieldMappings() {
		return fieldMappings;
	}

	public void setFieldMappings(Map<String, String> fieldMappings) {
		this.fieldMappings = fieldMappings;
	}

	public boolean isPartialKey() {
		return partialKey;
	}

	public void setPartialKey(boolean partialKey) {
		this.partialKey = partialKey;
	}
	

}
