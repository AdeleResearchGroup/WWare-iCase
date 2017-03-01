package de.mannheim.wifo2.iop.plugin;

import java.util.Map;

import de.mannheim.wifo2.iop.connection.IConnection;
import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.connection.impl.ConnectionManager;
import de.mannheim.wifo2.iop.connection.impl.MulticastGroup;
import de.mannheim.wifo2.iop.connection.impl.TCPClientConnection;
import de.mannheim.wifo2.iop.connection.impl.TCPServerConnection;
import de.mannheim.wifo2.iop.eventing.EventDispatcher;
import de.mannheim.wifo2.iop.eventing.IDynamicRouter;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.functions.application.IInvocation;
import de.mannheim.wifo2.iop.functions.application.Invocation;
import de.mannheim.wifo2.iop.functions.discovery.Announcement;
import de.mannheim.wifo2.iop.functions.discovery.IAnnouncement;
import de.mannheim.wifo2.iop.functions.discovery.ILookup;
import de.mannheim.wifo2.iop.functions.discovery.Lookup;
import de.mannheim.wifo2.iop.identifier.DeviceID;
import de.mannheim.wifo2.iop.identifier.IDeviceID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.identifier.PluginID;
import de.mannheim.wifo2.iop.location.Location;
import de.mannheim.wifo2.iop.registry.DeviceRegistry;
import de.mannheim.wifo2.iop.registry.IEndpointRegistry;
import de.mannheim.wifo2.iop.system.IEnqueue;
import de.mannheim.wifo2.iop.translation.MessageHandler;
import de.mannheim.wifo2.iop.util.Constants;
import de.mannheim.wifo2.iop.util.datastructure.Queue;
import de.mannheim.wifo2.iop.util.debug.DebugConstants;
import de.mannheim.wifo2.iop.util.debug.Log;

public class APlugin implements IPlugin, IEnqueue, Runnable {
	
	private IPluginID mPluginID;
	private IDynamicRouter<IEvent> mEventDispatcher;
	protected IEnqueue mMediator;
	protected IEndpointRegistry mDeviceRegistry;
	private Queue<IEvent> mEventQueue;
	protected IConnectionManager mConnectionManager;
	private Thread mThread;
	private volatile boolean mIsRunning;
	
	protected IAnnouncement mAnnouncement;
	protected ILookup mLookup;
	protected IInvocation mInvocation;
	
	public APlugin(String name, IEnqueue mediator, 
			Map<String, Object> properties)  {
		mIsRunning = false;
		
		IDeviceID deviceID = new DeviceID((String) properties.get(Constants.SELF_ID),
				new Location((String) properties.get(Constants.SELF_LOCATION)));
		mPluginID = new PluginID(name, deviceID);
		
		mMediator = mediator;
		mEventQueue = new Queue<>();
		mEventDispatcher = new EventDispatcher();
		mDeviceRegistry = new DeviceRegistry(mPluginID, this, mMediator);
				
		initializeConnectionManager(properties);
		
		initializeAnnouncementFunction(properties);
		initializeLookupFunction(properties);
		initializeInvocationFunction(properties);
		initializeAdditionalFunctions(properties);
		
		initializeChannels();
		
		initializeConnections(properties);
				
		mThread = null;
	}
	
	@Override
	public void enqueue(IEvent event) {
		if(mIsRunning)  {
			mEventQueue.enqueue(event);
		}
	}
	
	@Override
	public IPluginID getID()  {
		return mPluginID;
	}
	
	public IConnectionManager getConnectionManager()  {
		return mConnectionManager;
	}
	
	@Override
	public void start()  {
		if(DebugConstants.PLUGIN)
			Log.log(getClass(), mPluginID.getPluginName() + " is starting...");
		
		if(mThread == null)  {
			mThread = new Thread(this);
			mThread.setDaemon(true);
		}
		mIsRunning = true;
		mThread.start();
		
		if(mAnnouncement != null)  {
			mAnnouncement.start();
		}
		
		if(DebugConstants.PLUGIN)
			Log.log(getClass(), mPluginID.getPluginName() + " has been started.");
	}
	
	@Override
	public void stop()  {
		if(DebugConstants.PLUGIN)
			Log.log(getClass(), mPluginID.getPluginName() + " is stopping...");
		
		mIsRunning = false;
//		mThread = null;
		
		if(mAnnouncement != null)  {
			mAnnouncement.stop();
		}
		
		if(mConnectionManager != null)  {
			mConnectionManager.stopConnections();
		}
		
		if(mDeviceRegistry != null)  {
			mDeviceRegistry.stop();
		}
		
		if(DebugConstants.PLUGIN)
			Log.log(getClass(), mPluginID.getPluginName() + " has been stopped.");
	}
	
	@Override
	public void run()  {
		while(mIsRunning)  {
			if(!mEventQueue.isEmpty())  {				
				IEvent event = mEventQueue.dequeue();
				
				if(DebugConstants.PLUGIN)
					Log.log(getClass(), "Next event: " + event.toString());

				mEventDispatcher.dispatch(event);
			}
			else  {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	protected void initializeAnnouncementFunction(Map<String, Object> properties)  {
		mAnnouncement = new Announcement(mMediator, mPluginID, mMediator, mConnectionManager, (int) properties.get(Constants.ADVERTISEMENT_PERIOD));
	}
	
	protected void initializeLookupFunction(Map<String, Object> properties)  {
		mLookup = new Lookup(mMediator, mPluginID, mMediator, mConnectionManager, null);
	}
		
	protected void initializeInvocationFunction(Map<String, Object> properties)  {
		mInvocation = new Invocation(mMediator, mPluginID, mMediator, mConnectionManager);
	}
	
	protected void initializeAdditionalFunctions(Map<String, Object> properties)  {
		
	}
	
	protected void initializeChannels()  {
		if(mDeviceRegistry != null)  {
			mEventDispatcher.registerChannel(IEvent.EVENT_DEVICE_REGISTRATION, mDeviceRegistry);
			mEventDispatcher.registerChannel(IEvent.EVENT_DEVICE_DEREGISTRATION, mDeviceRegistry);
		}
		
		if(mAnnouncement != null)  {
			mEventDispatcher.registerChannel(IEvent.EVENT_ANNOUNCEMENT, mAnnouncement);
			mEventDispatcher.registerChannel(IEvent.EVENT_REGISTRATION, mAnnouncement);
		}
		
		if(mLookup != null)  {
			mEventDispatcher.registerChannel(IEvent.EVENT_LOOKUP, mLookup);
			mEventDispatcher.registerChannel(IEvent.EVENT_LOOKUPRESPONSE, mLookup);
		}
		
		if(mInvocation != null)  {
			mEventDispatcher.registerChannel(IEvent.EVENT_APPLICATION, mInvocation);
			mEventDispatcher.registerChannel(IEvent.EVENT_APPLICATIONRESPONSE, mInvocation);
		}
	}
	
	protected void initializeConnectionManager(Map<String, Object> properties)  {
		mConnectionManager = new ConnectionManager(this, mPluginID, new MessageHandler(), TCPClientConnection.class);
	}
	
	protected void initializeConnections(Map<String, Object> properties)  {

		String address = (String) properties.get(Constants.CONNECTION_ADVERTISEMENT_ADDRESS);
		Integer port = (Integer) properties.get(Constants.CONNECTION_ADVERTISEMENT_PORT);
		
		IDeviceID deviceID = new DeviceID(IConnectionManager.ADVERTISEMENT, 
				new Location(null, address, port, null));
		IConnection multicast = new MulticastGroup(mConnectionManager, address, port);

		mConnectionManager.addConnection(deviceID, multicast);
		multicast.start();
		
		port = (Integer) properties.get(Constants.CONNECTION_SERVER_PORT);

		IEndpointID deviceIDServer = new DeviceID(IConnectionManager.SERVER, 
				new Location(null, "127.0.0.1", port, null));
		IConnection server = new TCPServerConnection(mConnectionManager, TCPClientConnection.class, port);
		mConnectionManager.addConnection(deviceIDServer, server);
		server.start();
		
	}
}
