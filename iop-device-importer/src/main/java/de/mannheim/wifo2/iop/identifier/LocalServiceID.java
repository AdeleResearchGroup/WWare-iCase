package de.mannheim.wifo2.iop.identifier;

public class LocalServiceID implements ILocalServiceID  {
	private static final long serialVersionUID = -8465980456549082053L;
	
	private int mType;
	private IDeviceID mDeviceID;
	private String mObjectID;
	
	public LocalServiceID(IDeviceID deviceID, String objectID)  {
		mType = IID.ID_SERVICE;
		mDeviceID = deviceID;
		mObjectID = objectID;
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
	public String getObjectID() {
		return mObjectID;
	}

	@Override
	public IDeviceID getDeviceID() {
		return mDeviceID;
	}
	
	@Override
	public IEndpointID getEndpoint() {
		return mDeviceID;
	}

	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append(mDeviceID + "-");
		sb.append(mObjectID);
		
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mDeviceID == null) ? 0 : mDeviceID.hashCode());
		result = prime * result + ((mObjectID == null) ? 0 : mObjectID.hashCode());
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
		LocalServiceID other = (LocalServiceID) obj;
		if (mDeviceID == null) {
			if (other.mDeviceID != null)
				return false;
		} else if (!mDeviceID.equals(other.mDeviceID))
			return false;
		if (mObjectID == null) {
			if (other.mObjectID != null)
				return false;
		} else if (!mObjectID.equals(other.mObjectID))
			return false;
		return true;
	}
}
