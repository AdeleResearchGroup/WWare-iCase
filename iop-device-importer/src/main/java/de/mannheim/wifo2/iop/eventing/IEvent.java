package de.mannheim.wifo2.iop.eventing;

import java.io.Serializable;

import de.mannheim.wifo2.iop.identifier.IComponentID;

public interface IEvent extends Serializable {	
	public final static int EVENT_REGISTRATION = 1;
	public final static int EVENT_DEREGISTRATION = 2;
	public final static int EVENT_DEVICE_REGISTRATION = 3;
	public final static int EVENT_DEVICE_DEREGISTRATION = 4;
	
	public final static int EVENT_ROUTING = 7;
	
	public final static int EVENT_PLUGIN_ADDED = 10;
	public final static int EVENT_PLUGIN_REMOVED = 11;
	public final static int EVENT_PLUGIN_STARTED = 12;
	public final static int EVENT_PLUGIN_STOPPED = 13;
	
	public final static int EVENT_SERVICE_DESCRIPTION_ADDED = 15;
	public final static int EVENT_SERVICE_DESCRIPTION_REQUEST = 16;
	public final static int EVENT_SERVICE_DESCRIPTION_RESPONSE = 17;
	
	public final static int EVENT_CONTEXT_REPORT = 50;
	public final static int EVENT_CONTEXT_SIM_TIME_UPDATE = 70;
	
	//must be <= RESPONSE_BOUNDARY
	public final static int EVENT_ADVERTISEMENT = 101;
	public final static int EVENT_ANNOUNCEMENT = 102;
	public final static int EVENT_APPLICATION = 104;
	public final static int EVENT_LOOKUP = 103;
	
	//Responses must be > RESPONSE_BOUNDARY
	public final static int RESPONSE_BOUNDARY = 200;
	public final static int EVENT_LOOKUPRESPONSE = 203;
	public final static int EVENT_APPLICATIONRESPONSE = 204;
	
	//Plugin-specific events should be >= 1000
	
	public int getType();
	public Integer getID();
	public void setSource(IComponentID source);
	public IComponentID getSource();
}
