package de.mannheim.wifo2.iop.functions.discovery;

import java.util.Collections;

import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.event.i.IAnnouncementEvent;
import de.mannheim.wifo2.iop.event.i.IEventingEvent;
import de.mannheim.wifo2.iop.event.impl.AnnouncementEvent;
import de.mannheim.wifo2.iop.event.impl.EventingEvent;
import de.mannheim.wifo2.iop.eventing.EEventingType;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.identifier.impl.DeviceID;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;
import de.mannheim.wifo2.iop.util.i.IEnqueue;

public class SimpleAnnouncement extends AAnnouncement  {
	
	IEndpointID mTarget;
	
	public SimpleAnnouncement(IEnqueue queue, IPluginID pluginID,
			IEnqueue mediator, IConnectionManager connectionManager,
			int advertisementPeriod) {
		super(queue, pluginID, mediator, connectionManager, advertisementPeriod);
		
		mTarget = new DeviceID(IConnectionManager.ADVERTISEMENT, null);
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
	public void processAnnouncement(IEvent event) {
		IAnnouncementEvent announcementEvent = (IAnnouncementEvent) event;
		
		IEventingEvent deviceRegistrationEvent =
				new EventingEvent(mPluginID, EEventingType.DEVICE_REGISTRATION);
		deviceRegistrationEvent.addProperty(IEventingEvent.DEVICE_ID, 
				announcementEvent.getSourceID());
		deviceRegistrationEvent.addProperty(IEventingEvent.TIME_TO_LIVE, 
				announcementEvent.getTimeToLive()); 
		
		mQueue.enqueue(deviceRegistrationEvent);
		
		for(IServiceDescription service : announcementEvent.getServices())  {
			IEventingEvent registrationEvent = 
					new EventingEvent(mPluginID, EEventingType.SERVICE_REGISTRATION);
			registrationEvent.addProperty(IEventingEvent.SERVICE, service);
			
			mMediator.enqueue(registrationEvent);
		}
	}
}
