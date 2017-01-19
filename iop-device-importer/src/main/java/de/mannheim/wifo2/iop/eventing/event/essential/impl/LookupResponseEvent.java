package de.mannheim.wifo2.iop.eventing.event.essential.impl;

import java.util.List;

import de.mannheim.wifo2.iop.eventing.Event;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.ILookupResponseEvent;
import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

public class LookupResponseEvent extends Event implements ILookupResponseEvent  {
	private static final long serialVersionUID = -4508050191072213899L;

	private IEndpointID mTarget;
	private IEndpointID mSource;
	private List<? extends IServiceDescription> mServices;
	
	public LookupResponseEvent(IComponentID sourceID, 
			Integer id, IEndpointID sourceDevice, IEndpointID targetDevice, 
			List<? extends IServiceDescription> services) {
		super(IEvent.EVENT_LOOKUPRESPONSE, id, sourceID);
		
		mSource = sourceDevice;
		mTarget = targetDevice;
		mServices = services;
	}
	
	public List<? extends IServiceDescription> getServices()  {
		return mServices;
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
		sb.append(mServices);
		sb.append(")");
		
		return sb.toString();
	}
}
