package de.mannheim.wifo2.iop.eventing.event.essential.impl;

import java.util.List;

import de.mannheim.wifo2.iop.eventing.Event;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IAdvertisementEvent;
import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

public class AdvertisementEvent extends Event 
		implements IAdvertisementEvent {
	private static final long serialVersionUID = 4867951715464730667L;
	
	private IEndpointID mEndpointID;
	private List<? extends IServiceDescription> mServices;
	
	public AdvertisementEvent(IComponentID sourceID,
			IEndpointID endpoint, List<? extends IServiceDescription> services) {
		super(IEvent.EVENT_ADVERTISEMENT, null, sourceID);
		
		mEndpointID = endpoint;
		mServices = services;
	}

	public IEndpointID getEndpointID()  {
		return mEndpointID;
	}
	
	public List<? extends IServiceDescription> getServices()  {
		return mServices;
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
