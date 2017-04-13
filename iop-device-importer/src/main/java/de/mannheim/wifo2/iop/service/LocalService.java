package de.mannheim.wifo2.iop.service;

import java.util.List;

import de.mannheim.wifo2.iop.identifier.ILocalServiceID;
import de.mannheim.wifo2.iop.service.ILocalServiceDescription;
import de.mannheim.wifo2.iop.service.model.IFunctionality;
import de.mannheim.wifo2.iop.service.model.IProperty;

public class LocalService implements ILocalServiceDescription  {
	private static final long serialVersionUID = -3018426628296468830L;
	
	private ILocalServiceID mId;
	private String mName;
	private List<IFunctionality> mFunctionalities;
	private List<IProperty> mProperties;
	
	public LocalService(ILocalServiceID id, String name, 
			List<IFunctionality> functionalities, 
			List<IProperty> properties) {
		mId = id;
		mName = name;
		mFunctionalities = functionalities;
		mProperties = properties;
	}

	@Override
	public ILocalServiceID getId() {
		return mId;
	}

	@Override
	public String getName() {
		return mName;
	}
	
	@Override
	public List<IFunctionality> getFunctionalities() {
		return mFunctionalities;
	}

	@Override
	public List<IProperty> getProperties() {
		return mProperties;
	}

	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("LocalService(");
		sb.append(mId + ", ");
		sb.append(mName + ", ");
		sb.append(mFunctionalities + ", ");
		sb.append(mProperties);
		sb.append(")");
		
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mId == null) ? 0 : mId.hashCode());
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
		LocalService other = (LocalService) obj;
		if (mId == null) {
			if (other.mId != null)
				return false;
		} else if (!mId.equals(other.mId))
			return false;
		return true;
	}
}
