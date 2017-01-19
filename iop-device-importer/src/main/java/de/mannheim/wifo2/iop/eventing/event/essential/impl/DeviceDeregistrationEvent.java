package de.mannheim.wifo2.iop.eventing.event.essential.impl;

import de.mannheim.wifo2.iop.eventing.Event;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;

public class DeviceDeregistrationEvent extends Event {
	private static final long serialVersionUID = -4620685330801827574L;
	
	private IEndpointID mDeviceID;

	public DeviceDeregistrationEvent(IComponentID sourceID, 
			IEndpointID deviceID) {
		super(IEvent.EVENT_DEVICE_DEREGISTRATION, null, sourceID);

		mDeviceID = deviceID;
	}

	public IEndpointID getDeviceID()  {
		return mDeviceID;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("Event(");
		sb.append(getType() + ", ");
		sb.append(mDeviceID);
		sb.append(")");
		
		return sb.toString();
	}
}
