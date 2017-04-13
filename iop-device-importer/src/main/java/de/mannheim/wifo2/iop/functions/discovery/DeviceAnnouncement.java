package de.mannheim.wifo2.iop.functions.discovery;

import java.util.Collections;
import java.util.Vector;

import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.event.i.IAnnouncementEvent;
import de.mannheim.wifo2.iop.event.i.IEventingEvent;
import de.mannheim.wifo2.iop.event.i.ILookupEvent;
import de.mannheim.wifo2.iop.event.impl.AnnouncementEvent;
import de.mannheim.wifo2.iop.event.impl.LookupEvent;
import de.mannheim.wifo2.iop.eventing.EEventingType;
import de.mannheim.wifo2.iop.plugin.function.matching.IMatchRequest;
import de.mannheim.wifo2.iop.identifier.IDeviceID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.identifier.impl.DeviceID;
import de.mannheim.wifo2.iop.plugin.function.matching.impl.SimpleMatchRequest;
import de.mannheim.wifo2.iop.util.i.IEnqueue;

public class DeviceAnnouncement extends SimpleAnnouncement {

	private IEndpointID mTarget;
	Vector<IDeviceID> mDevices;
	
	public DeviceAnnouncement(IEnqueue queue, IPluginID pluginID, 
			IEnqueue mediator, IConnectionManager connectionManager, 
			int advertisementPeriod) {
		super(queue, pluginID, mediator, connectionManager, advertisementPeriod);

		mTarget = new DeviceID(IConnectionManager.ADVERTISEMENT, null);
		mDevices = new Vector<>();
	}
	
	@Override
	public void dispatch(IEvent event)  {
		super.dispatch(event);
		
		int type = event.getType();
		
		if(type == IEvent.EVENT_ANNOUNCEMENT)  {
			IAnnouncementEvent announcementEvent = (IAnnouncementEvent) event;
			if(!mDevices.contains(announcementEvent.getSourceID()))  {
				IDeviceID deviceID = (IDeviceID) announcementEvent.getSourceID();
				IMatchRequest request = new SimpleMatchRequest();
				ILookupEvent lookupEvent = new LookupEvent(mPluginID, -2147483648,
						mPluginID.getDeviceID(), deviceID, request);
				
//				mQueue.enqueue(lookupEvent);
				
//				mDevices.addElement(deviceID);
				
				mConnectionManager.send(lookupEvent);
			}
		}
		else if(type == IEvent.EVENT_EVENTING)  {
			IEventingEvent eventingEvent = (IEventingEvent) event;
			if(eventingEvent.getEventingType() == EEventingType.DEVICE_DEREGISTRATION)  {
				mDevices.remove(eventingEvent.getProperty(IEventingEvent.DEVICE_ID));
			}
		}
	}
	
	@Override
	public void announce() {
		IAnnouncementEvent event = 
				new AnnouncementEvent(mPluginID, 
						mPluginID.getDeviceID(), 
						mTarget,
						Collections.emptyList(), 30000);	//TODO dynamic
		
		mConnectionManager.send(event);
	}

	@Override
	public void stop()  {
		super.stop();
		
		mDevices.clear();
	}
}
