package de.mannheim.wifo2.iop.event.impl;

import java.util.List;

import de.mannheim.wifo2.iop.event.Event;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.event.i.IAnnouncementEvent;
import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

public class AnnouncementEvent extends Event 
		implements IAnnouncementEvent  {
	private static final long serialVersionUID = -4179662511078527166L;

	private IEndpointID mSource;
	private IEndpointID mTarget;
	private long mTimeToLive;
	private List<? extends IServiceDescription> mServices;
	
	public AnnouncementEvent(IComponentID sourceID,
			IEndpointID sourceDevice, IEndpointID targetDevice, 
			List<? extends IServiceDescription> services, 
			long timeToLive) {
		super(IEvent.EVENT_ANNOUNCEMENT, null, sourceID);
		
		mSource = sourceDevice;
		mTarget = targetDevice;
		mServices = services;
		mTimeToLive = timeToLive;
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
	public IEndpointID getSourceID() {
		return mSource;
	}
	
	@Override
	public IEndpointID getTargetID() {
		return mTarget;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("Event(");
		sb.append(getType() + ", ");
		sb.append(mSource + ", ");
		sb.append(mServices);
		sb.append(")");
		
		return sb.toString();
	}
}
