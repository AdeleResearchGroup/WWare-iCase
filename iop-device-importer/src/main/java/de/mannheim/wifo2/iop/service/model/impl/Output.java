package de.mannheim.wifo2.iop.service.model.impl;

import de.mannheim.wifo2.iop.service.model.IOutput;

public class Output implements IOutput  {
	private static final long serialVersionUID = 587431860073728077L;
	
	private String mName;
	private String mType;

	public Output(String name, String type)  {
		mName = name;
		mType = type;
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
