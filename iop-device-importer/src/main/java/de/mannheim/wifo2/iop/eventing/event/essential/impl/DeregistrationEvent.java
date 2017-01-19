package de.mannheim.wifo2.iop.eventing.event.essential.impl;

import de.mannheim.wifo2.iop.eventing.Event;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IDeregistrationEvent;
import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.identifier.IID;

public class DeregistrationEvent extends Event 
		implements IDeregistrationEvent {
	private static final long serialVersionUID = -3472196680427145810L;
	
	private IID mId;

	public DeregistrationEvent(IComponentID sourceID, IID id) {
		super(IEvent.EVENT_DEREGISTRATION, null, sourceID);
		mId = id;
	}

	@Override
	public IID getServiceID()  {
		return mId;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("Event(");
		sb.append(getType() + ", ");
		sb.append(mId);
		sb.append(")");
		
		return sb.toString();
	}
}
