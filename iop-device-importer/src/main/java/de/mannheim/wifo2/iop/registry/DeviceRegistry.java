package de.mannheim.wifo2.iop.registry;

import java.util.Vector;

import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.event.i.IEventingEvent;
import de.mannheim.wifo2.iop.event.impl.EventingEvent;
import de.mannheim.wifo2.iop.eventing.EEventingType;
import de.mannheim.wifo2.iop.identifier.IDeviceID;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.registry.IEndpointRegistry;
import de.mannheim.wifo2.iop.util.debug.DebugConstants;
import de.mannheim.wifo2.iop.util.debug.Log;
import de.mannheim.wifo2.iop.util.i.IEnqueue;
import de.mannheim.wifo2.iop.util.i.IStop;

public class DeviceRegistry implements IEndpointRegistry, Runnable, IStop {

	protected IEnqueue mQueue;
	protected IPluginID mPluginID;
	protected IEnqueue mMediator;
	private Vector<IDeviceID> mDeviceRegistry;
	private Vector<Long> mExpiration;
	private long mNextCheck;
	private Thread mThread;
	private volatile boolean mIsRunning;
	
	public DeviceRegistry(IPluginID pluginID, IEnqueue queue,
			IEnqueue mediator) {
		mQueue = queue;
		mPluginID = pluginID;
		mMediator = mediator;
		mDeviceRegistry = new Vector<>();
		mExpiration = new Vector<>();
		mNextCheck = 2000L;
		mIsRunning = false;
		
		mThread = new Thread(this);
		mThread.setDaemon(true);
		mThread.start();
	}
	
	@Override
	public void dispatch(IEvent event) {
		int type = event.getType();
		
		if(type == IEvent.EVENT_EVENTING)  {
			IEventingEvent eventingEvent = (IEventingEvent) event;
			if(eventingEvent.getEventingType() == EEventingType.DEVICE_REGISTRATION)  {
				IDeviceID deviceID = (IDeviceID) eventingEvent.getProperty(IEventingEvent.DEVICE_ID);;
				
				synchronized (mDeviceRegistry) {
					int index = mDeviceRegistry.indexOf(deviceID);
					long now = System.currentTimeMillis();
					
					if(index != -1)  {
						mExpiration.set(index, 
								now + Long.valueOf(eventingEvent.getProperty(IEventingEvent.TIME_TO_LIVE)+""));
					}
					else  {
						mDeviceRegistry.add(deviceID);
						mExpiration.add(now + Long.valueOf(eventingEvent.getProperty(IEventingEvent.TIME_TO_LIVE)+""));
					}
				}
			}
			else if(eventingEvent.getEventingType() == EEventingType.DEVICE_DEREGISTRATION)  {
				IDeviceID deviceID = (IDeviceID) eventingEvent.getProperty(IEventingEvent.DEVICE_ID);
				
				synchronized (mDeviceRegistry) {
					int index = mDeviceRegistry.indexOf(deviceID);
					
					if(index != -1)  {
						mDeviceRegistry.remove(index);
						mExpiration.remove(index);
					}
				}
			}
		}
		
		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public void run() {
		mIsRunning = true;
		
		while(mIsRunning)  {
			mNextCheck = refreshRegistry();
			
			try  {
				if(DebugConstants.ADVERTISEMENT)
					Log.log(getClass(), "Next Check: " + mNextCheck + " ms");
				synchronized (this) {
					this.wait(mNextCheck);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private long refreshRegistry()  {
		long nextCheck = 600000L;
		
		synchronized(mDeviceRegistry)  {
			Vector<Integer> removeIndices = new Vector<>();
			long now = System.currentTimeMillis();
			
			for(int i=0 ; i<mDeviceRegistry.size() ; i++)  {
				long diff = mExpiration.get(i) - now;
				
				if(diff <= 0)  {
					if(DebugConstants.ADVERTISEMENT)  
						Log.log(getClass(), "Device expired: " + mDeviceRegistry.get(i).getID());

					removeIndices.add(i);
					
					IEventingEvent deviceDeregistrationEvent = 
							new EventingEvent(mPluginID, EEventingType.DEVICE_DEREGISTRATION);
					deviceDeregistrationEvent.addProperty(IEventingEvent.DEVICE_ID, mDeviceRegistry.get(i));
					
					mQueue.enqueue(deviceDeregistrationEvent);
					mMediator.enqueue(deviceDeregistrationEvent);
				}
				else  {
					if(diff < nextCheck)  {
						nextCheck = diff;
					}
				}
			}
			
			for(Integer i : removeIndices)  {
				mDeviceRegistry.remove(i);
				mExpiration.remove(i);
			}
			
			return nextCheck;
		}
	}

	@Override
	public void stop() {
		mIsRunning = false;
//		mThread = null;
	}
}
