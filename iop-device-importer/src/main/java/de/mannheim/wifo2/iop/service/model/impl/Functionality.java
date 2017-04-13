package de.mannheim.wifo2.iop.service.model.impl;

import java.util.List;
import java.util.Vector;

import de.mannheim.wifo2.iop.service.model.ICapability;
import de.mannheim.wifo2.iop.service.model.IFunctionality;
import de.mannheim.wifo2.iop.service.model.IProperty;

public class Functionality implements IFunctionality  {
	private static final long serialVersionUID = 2893719812426893981L;

	private String mName;
	private List<ICapability> mCapabilities;
	private List<IProperty> mProperties;
	
	public Functionality(String name)  {
		mName = name;
		mCapabilities = new Vector<>();
		mProperties = new Vector<>();
	}
	
	public Functionality(String name, 
			List<ICapability> capabilities,
			List<IProperty> properties) {
		mName = name;
		
		if(capabilities == null)  {
			mCapabilities = new Vector<>();
		}
		else  {
			mCapabilities = capabilities;
		}
		
		if(properties == null)  {
			mProperties = new Vector<>();
		}
		else  {
			mProperties = properties;
		}
	}
	
	@Override
	public String getName() {
		return mName;
	}

	@Override
	public List<ICapability> getCapabilities() {
		return mCapabilities;
	}

	@Override
	public List<IProperty> getProperties() {
		return mProperties;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("Functionality(" + mName + ": ");
		for(ICapability c : mCapabilities)  {
			sb.append(c + ", ");
		}
		for(IProperty p : mProperties)  {
			sb.append(p + ", ");
		}
		sb.append(")");
		
		return sb.toString();
	}
}
