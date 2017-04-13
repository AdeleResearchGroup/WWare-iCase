package de.mannheim.wifo2.iop.plugin.function.matching.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.mannheim.wifo2.iop.plugin.function.matching.IMatchRequest;

public class SimpleMatchRequest implements IMatchRequest {
	private static final long serialVersionUID = 7770099491012243487L;
	
	private Map<String, Object> mProperties;
	
	public SimpleMatchRequest() {
		mProperties = new HashMap<>();
	}
	
	public SimpleMatchRequest(String[] functionality)  {
		this();
		mProperties.put(IMatchRequest.FUNCTIONALITY, functionality);
	}
	
	@Override
	public Object getProperty(String key)  {
		return mProperties.get(key);
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("Request (");
		for(Entry<String, Object> e : mProperties.entrySet())  {
			sb.append(e.getKey() + ":" + e.getValue() + ", ");
		}
		sb.append(")");
		return sb.toString();
	}
}
