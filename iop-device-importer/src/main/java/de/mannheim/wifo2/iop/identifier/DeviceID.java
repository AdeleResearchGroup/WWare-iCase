package de.mannheim.wifo2.iop.identifier;

import de.mannheim.wifo2.iop.location.ILocation;

public class DeviceID implements IDeviceID {
	private static final long serialVersionUID = 7544563172045288777L;

	private String mID;
	private int mType;
	private ILocation mLocation;
	
	public DeviceID(String id, ILocation location)  {
		mType = IID.ID_DEVICE;
		mID = id;
		mLocation = location;
	}
	
	@Override
	public String getID() {
		return mID;
	}
	
	@Override
	public int getType()  {
		return mType;
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
	public String toString()  {
		return "Device(" + mID + ", " + mLocation + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mID == null) ? 0 : mID.hashCode());
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
		DeviceID other = (DeviceID) obj;
		if (mID == null) {
			if (other.mID != null)
				return false;
		} else if (!mID.equals(other.mID))
			return false;
		return true;
	}
}
