package de.mannheim.wifo2.iop.event.i;

import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.eventing.EEventingType;

public interface IEventingEvent extends IEvent  {
	public final static String SERVICE = "service";
	public final static String SERVICE_ID = "serviceID";
	public final static String DEVICE_ID = "deviceID";
	public final static String VARIABLE = "variable";
	public final static String NEW_STATE = "newState";
	public final static String TIME_TO_LIVE = "ttl";
	
	public EEventingType getEventingType();
	public void addProperty(String key, Object value);
	public Object getProperty(String key);
}
