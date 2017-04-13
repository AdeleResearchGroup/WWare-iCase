package de.mannheim.wifo2.iop.identifier.impl;

import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.IID;
import de.mannheim.wifo2.iop.identifier.ISystemID;
import de.mannheim.wifo2.iop.location.ILocation;

public class SystemID implements ISystemID {
	private static final long serialVersionUID = 6980026671093035984L;

	private int mType;
	private String mName;
	private ILocation mLocation;
	
	public SystemID(String name, ILocation location) {
		mType = IID.ID_SYSTEM;
		mName = name;
		mLocation = location;
	}
	
	@Override
	public String getID() {
		return this.toString();
	}

	@Override
	public int getType()  {
		return mType;
	}

	@Override
	public String getSystemName() {
		return mName;
	}
	
	@Override
	public ILocation getLocation() {
		return mLocation;
	}
	
	@Override
	public IEndpointID getEndpoint() {
		return this;
	}
	
	@Override
	public String toString() {
		return "SystemID(" + mName + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mLocation == null) ? 0 : mLocation.hashCode());
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
		SystemID other = (SystemID) obj;
		if (mLocation == null) {
			if (other.mLocation != null)
				return false;
		} else if (!mLocation.equals(other.mLocation))
			return false;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		return true;
	}
}
