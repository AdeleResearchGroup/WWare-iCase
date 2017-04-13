package de.mannheim.wifo2.iop.identifier.impl;

import de.mannheim.wifo2.iop.identifier.IDeviceID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.IID;
import de.mannheim.wifo2.iop.identifier.IPluginID;

public class PluginID implements IPluginID {
	private static final long serialVersionUID = 177087740884857726L;
	
	private int mType;
	private String mName;
	private IDeviceID mDeviceID;
	
	public PluginID(String name, IDeviceID deviceID) {
		mType = IID.ID_PLUGIN;
		mName = name;
		mDeviceID = deviceID;
	}
	
	@Override
	public String getPluginName() {
		return mName;
	}

	@Override
	public IDeviceID getDeviceID() {
		return mDeviceID;
	}
	
	@Override
	public String getID()  {
		return this.toString();
	}

	@Override
	public int getType()  {
		return mType;
	}
	
	@Override
	public IEndpointID getEndpoint() {
		return null;
	}

	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("PluginID(");
		sb.append(mName + ", ");
		sb.append(mDeviceID);
		sb.append(")");
		
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mDeviceID == null) ? 0 : mDeviceID.hashCode());
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PluginID other = (PluginID) obj;
		if (mDeviceID == null) {
			if (other.mDeviceID != null)
				return false;
		} else if (!mDeviceID.equals(other.mDeviceID))
			return false;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		return true;
	}
}
