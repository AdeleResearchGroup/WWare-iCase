package de.mannheim.wifo2.iop.service.model.impl;

import de.mannheim.wifo2.iop.service.model.IInput;

public class Input implements IInput  {
	private static final long serialVersionUID = 3583956030764254810L;
	
	private String mName;
	private String mType;

	public Input(String name, String type)  {
		mName = name;
	}
	
	@Override
	public String getName() {
		return mName;
	}

	@Override
	public String getType() {
		return mType;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append(mName);
		sb.append("(" + mType + ")");
		
		return sb.toString();
	}
}
