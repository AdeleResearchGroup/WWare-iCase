package de.mannheim.wifo2.iop.functions.discovery;

import java.util.Collections;
import java.util.Vector;

import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.eventing.event.EventID;
import de.mannheim.wifo2.iop.eventing.event.essential.IAnnouncementEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.ILookupEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IRegistrationEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.AnnouncementEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.DeviceRegistrationEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.LookupEvent;
import de.mannheim.wifo2.iop.functions.discovery.IAnnouncement;
import de.mannheim.wifo2.iop.functions.matching.IMatchRequest;
import de.mannheim.wifo2.iop.functions.matching.SimpleMatchRequest;
import de.mannheim.wifo2.iop.identifier.IDeviceID;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.service.LocalService;
import de.mannheim.wifo2.iop.system.IEnqueue;

public class Announcement implements IAnnouncement, Runnable {
	private IEnqueue mQueue;
	private IPluginID mSystemID;
	@SuppressWarnings("unused")
	private IEnqueue mMediator;
	private IConnectionManager mConnectionManager;
	private int mAdvertisementPeriod;
	private LocalService localService;
	
	Vector<IDeviceID> mKnownSystems;
	
	private Thread mThread;
	private volatile boolean mIsRunning;
	
	public Announcement(IEnqueue queue, IPluginID systemID,
			IEnqueue mediator, IConnectionManager connectionManager,
			int advertisementPeriod) {
		mIsRunning = false;
		mQueue = queue;
		mSystemID = systemID;
		mMediator = mediator;
		mConnectionManager = connectionManager;
		
		mKnownSystems = new Vector<>();
		mAdvertisementPeriod = advertisementPeriod;
		
		mThread = null;
		localService = null;
	}
	
	@Override
	public void start()  {
		if(mThread == null)  {
			mThread = new Thread(this);
			mThread.setDaemon(true);
		}
		mIsRunning = true;
		mThread.start();
	}
	
	@Override
	public void stop() {
		mIsRunning = false;
//		mThread = null;
	}
	
	@Override
	public void dispatch(IEvent event) {
		int type = event.getType();
		
		if(type == IEvent.EVENT_ANNOUNCEMENT)  {
			processAnnouncement(event);
		}
		else if (type == IEvent.EVENT_REGISTRATION) {
			processRegistration((IRegistrationEvent)event);
		}
//		else if(type == IEvent.EVENT_DEVICE_DEREGISTRATION)  {
//			DeviceDeregistrationEvent deregistrationEvent = 
//					(DeviceDeregistrationEvent) event;
//			
//			mKnownSystems.remove(deregistrationEvent.getDeviceID());
//		}
	}

	private void processRegistration(IRegistrationEvent event) {
		localService = (LocalService) event.getService();	
	}

	@Override
	public void run() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		while(mIsRunning)  {
			announce();
			
			try {
				Thread.sleep(mAdvertisementPeriod);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void announce() {
		IAnnouncementEvent event = 
				new AnnouncementEvent(mSystemID, 
						mSystemID.getDeviceID(), localService != null ? Collections.singletonList(localService) : Collections.emptyList(), -1);
		mConnectionManager.send(event);
	}

	@Override
	public void processAnnouncement(IEvent event) {
		IAnnouncementEvent announcementEvent = (IAnnouncementEvent) event;
		
		//TODO or better enqueue announcement event?
		
		DeviceRegistrationEvent deviceRegistrationEvent = 
				new DeviceRegistrationEvent(mSystemID, 
						announcementEvent.getEndpointID(), 18000);
		
		mQueue.enqueue(deviceRegistrationEvent);
		
//		if(!mKnownSystems.contains(announcementEvent.getEndpointID()))  {			
			IDeviceID deviceID = (IDeviceID) announcementEvent.getEndpointID();
			IMatchRequest request = new SimpleMatchRequest();
			ILookupEvent lookupEvent = new LookupEvent(mSystemID, EventID.getInstance().getNextID(),
					mSystemID.getDeviceID(), deviceID, request);
			
			mKnownSystems.addElement(deviceID);
			
			mConnectionManager.send(lookupEvent);	
//		}
	}
}
