package de.mannheim.wifo2.iop.eventing.event.essential.impl;

import de.mannheim.wifo2.iop.eventing.Event;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IRegistrationEvent;
import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

public class RegistrationEvent extends Event 
		implements IRegistrationEvent {
	private static final long serialVersionUID = 6146416607426834437L;
	
	private IServiceDescription mService;

	public RegistrationEvent(IComponentID sourceID, 
			IServiceDescription service) {
		super(IEvent.EVENT_REGISTRATION, null, sourceID);
		mService = service;
	}

	@Override
	public IServiceDescription getService()  {
		return mService;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("Event(");
		sb.append(getType() + ", ");
		sb.append(mService);
		sb.append(")");
		
		return sb.toString();
	}
}
