package de.mannheim.wifo2.iop.functions.discovery;

import java.util.Collections;
import java.util.Vector;

import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.event.i.IAnnouncementEvent;
import de.mannheim.wifo2.iop.event.i.IEventingEvent;
import de.mannheim.wifo2.iop.event.impl.AnnouncementEvent;
import de.mannheim.wifo2.iop.event.impl.EventingEvent;
import de.mannheim.wifo2.iop.eventing.EEventingType;
import de.mannheim.wifo2.iop.functions.discovery.IAnnouncement;
import de.mannheim.wifo2.iop.identifier.IDeviceID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.identifier.impl.DeviceID;
import de.mannheim.wifo2.iop.util.i.IEnqueue;

public class Announcement implements IAnnouncement, Runnable {
	private IEnqueue mQueue;
	private IPluginID mSystemID;
	@SuppressWarnings("unused")
	private IEnqueue mMediator;
	private IConnectionManager mConnectionManager;
	private int mAdvertisementPeriod;
	
	Vector<IDeviceID> mKnownSystems;
	
	private IEndpointID mTarget;
	
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
		
		mTarget = new DeviceID(IConnectionManager.ADVERTISEMENT, null);
		
		mThread = null;
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
						mSystemID.getDeviceID(), 
						mTarget,
						/*localService != null ? Collections.singletonList(localService) : */Collections.emptyList(), -1);
		mConnectionManager.send(event);
	}

	@Override
	public void processAnnouncement(IEvent event) {
		IAnnouncementEvent announcementEvent = (IAnnouncementEvent) event;
		
		//TODO or better enqueue announcement event?
		
		IEventingEvent deviceRegistrationEvent =
				new EventingEvent(mSystemID, EEventingType.DEVICE_REGISTRATION);
		deviceRegistrationEvent.addProperty(IEventingEvent.DEVICE_ID, 
				announcementEvent.getSourceID());
		deviceRegistrationEvent.addProperty(IEventingEvent.TIME_TO_LIVE, 18000); 	//TODO TTL dynamic!!!
		
		mQueue.enqueue(deviceRegistrationEvent);
		
//		if(!mKnownSystems.contains(announcementEvent.getEndpointID()))  {			
//			IDeviceID deviceID = (IDeviceID) announcementEvent.getSourceID();
//			IMatchRequest request = new SimpleMatchRequest();
//			ILookupEvent lookupEvent = new LookupEvent(mSystemID, EventID.getInstance().getNextID(),
//					mSystemID.getDeviceID(), deviceID, request);
//			
//			mKnownSystems.addElement(deviceID);
//			
//			mConnectionManager.send(lookupEvent);	
//		}
	}
}
