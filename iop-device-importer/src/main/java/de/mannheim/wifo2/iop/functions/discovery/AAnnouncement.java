package de.mannheim.wifo2.iop.functions.discovery;

import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.system.IEnqueue;

public abstract class AAnnouncement implements IAnnouncement, Runnable  {
	protected IEnqueue mQueue;
	protected IPluginID mPluginID;
	protected IEnqueue mMediator;
	protected IConnectionManager mConnectionManager;
	private int mAdvertisementPeriod;
	
	private Thread mThread;
	private volatile boolean mIsRunning;
	
	public AAnnouncement(IEnqueue queue, IPluginID pluginID, 
			IEnqueue mediator,
			IConnectionManager connectionManager,
			int advertisementPeriod) {
		mIsRunning = false;
		mQueue = queue;
		mPluginID = pluginID;
		mMediator = mediator;
		mConnectionManager = connectionManager;
		mAdvertisementPeriod = advertisementPeriod;
		
		mThread = new Thread(this);
	}
	
	@Override
	public void start()  {
		if(mThread == null)  {
			mThread = new Thread(this);
		}
		mIsRunning = true;
		mThread.start();
	}
	
	@Override
	public void stop() {
		mIsRunning = false;
		mThread = null;		
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
			Thread.sleep(2000);	//TODO in configuration?
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
}
