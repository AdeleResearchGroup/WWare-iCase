package de.mannheim.wifo2.iop.eventing;

import de.mannheim.wifo2.iop.identifier.IComponentID;

public abstract class Event implements IEvent {
	private static final long serialVersionUID = -6531300603665031701L;
	
	private int mType;
	private Integer mID;
	private IComponentID mSourceID;
	
	public Event(int type, Integer id,
			IComponentID sourceID)  {
		mType = type;
		mSourceID = sourceID;
		mID = id;
	}
	
	@Override
	public int getType() {
		return mType;
	}
	
	@Override
	public Integer getID()  {
		return mID;
	}
	
	@Override
	public void setSource(IComponentID sourceID)  {
		mSourceID = sourceID;
	}
	
	@Override
	public IComponentID getSource() {
		return mSourceID;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("Event(");
		sb.append(mType + ", ");
//		sb.append(mSourceID + ", ");
		sb.append(")");
		
		return sb.toString();
	}
}
