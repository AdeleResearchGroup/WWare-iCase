package de.mannheim.wifo2.iop.eventing.event.essential.impl;

import de.mannheim.wifo2.iop.eventing.Event;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;

public class DeviceRegistrationEvent extends Event {
	private static final long serialVersionUID = -7651640132242021069L;

	private IEndpointID mDeviceID;
	private long mTimeToLive;

	public DeviceRegistrationEvent(IComponentID sourceID, 
			IEndpointID deviceID, long timeToLive) {
		super(IEvent.EVENT_DEVICE_REGISTRATION, null, sourceID);

		mDeviceID = deviceID;
		mTimeToLive = timeToLive;
	}
	
	public IEndpointID getDeviceID()  {
		return mDeviceID;
	}
	
	public long getTimeToLive()  {
		return mTimeToLive;
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
