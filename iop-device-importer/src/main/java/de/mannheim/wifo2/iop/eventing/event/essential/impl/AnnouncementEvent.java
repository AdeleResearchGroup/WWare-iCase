package de.mannheim.wifo2.iop.eventing.event.essential.impl;

import java.util.List;

import de.mannheim.wifo2.iop.eventing.Event;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IAnnouncementEvent;
import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

public class AnnouncementEvent extends Event 
		implements IAnnouncementEvent  {
	private static final long serialVersionUID = -4179662511078527166L;

	private IEndpointID mEndpointID;
	private long mTimeToLive;
	private List<? extends IServiceDescription> mServices;
	
	public AnnouncementEvent(IComponentID sourceID,
			IEndpointID endpoint, List<? extends IServiceDescription> services, 
			long timeToLive) {
		super(IEvent.EVENT_ANNOUNCEMENT, null, sourceID);
		
		mEndpointID = endpoint;
		mServices = services;
		mTimeToLive = timeToLive;
	}

	@Override
	public IEndpointID getEndpointID()  {
		return mEndpointID;
	}
	
	@Override
	public List<? extends IServiceDescription> getServices()  {
		return mServices;
	}
	
	@Override
	public long getTimeToLive()  {
		return mTimeToLive;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("Event(");
		sb.append(getType() + ", ");
		sb.append(mEndpointID + ", ");
		sb.append(mServices);
		sb.append(")");
		
		return sb.toString();
	}
}
