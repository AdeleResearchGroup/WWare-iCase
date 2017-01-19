package de.mannheim.wifo2.iop.service.functionality;

import java.io.Serializable;

public class Parameter implements IParameter, Serializable {
	private static final long serialVersionUID = -5637913264725477240L;
	
	private String mKey;
	private Class<? extends Object> mClazz;
	private Object mValue;
	private String mMetaInfo;
	
	public Parameter(String key, Object value)  {
		this(key, value, null);
	}
	
	public Parameter(String key, Object value, String metaInfo)  {
		mKey = key;
		mValue = value;
		if(mValue != null)  {
			mClazz = mValue.getClass();
		}
		else  {
			mClazz = null;
		}
		mMetaInfo = metaInfo;
	}

	@Override
	public String getKey() {
		return mKey;
	}

	@Override
	public Class<? extends Object> getClazz() {
		return mClazz;
	}

	@Override
	public Object getValue() {
		return mValue;
	}
	
	@Override
	public boolean isMiddlewareSpecific() {
		if(mMetaInfo != null)  {
			return true;
		}
		return false;
	}

	@Override
	public String getMetaInformation() {
		return mMetaInfo;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append(mKey + " : ");
		sb.append(mValue);
		if(mClazz != null)  {
			sb.append(" (" + mClazz.getSimpleName() + ")");
		}
		if(isMiddlewareSpecific())  {
			sb.append(" - " + mMetaInfo);
		}
		return sb.toString();
	}
}
