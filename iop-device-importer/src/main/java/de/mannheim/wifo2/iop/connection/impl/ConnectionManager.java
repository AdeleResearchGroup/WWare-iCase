package de.mannheim.wifo2.iop.connection.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import de.mannheim.wifo2.iop.connection.ConnectionTable;
import de.mannheim.wifo2.iop.connection.IConnection;
import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.connection.IConnectionTable;
import de.mannheim.wifo2.iop.eventing.IDirectedEvent;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IAdvertisementEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IAnnouncementEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IApplicationEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IApplicationResponseEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IDeregistrationEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.ILookupEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.ILookupResponseEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IRegistrationEvent;
import de.mannheim.wifo2.iop.identifier.DeviceID;
import de.mannheim.wifo2.iop.identifier.IDeviceID;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.system.IEnqueue;
import de.mannheim.wifo2.iop.translation.IMessageHandler;
import de.mannheim.wifo2.iop.util.debug.DebugConstants;
import de.mannheim.wifo2.iop.util.debug.Log;

public class ConnectionManager implements IConnectionManager  {		
	private IConnectionTable mConnections;
	private IEnqueue mQueue;
	private IPluginID mPluginID;
	private Class<IConnection> mClass;
	private IMessageHandler mMessageHandler;
	
	@SuppressWarnings("unchecked")
	public ConnectionManager(IEnqueue queue, IPluginID pluginID,
			IMessageHandler messageHandler,
			Class<? extends IConnection> clazz) {
		mQueue = queue;
		mConnections = new ConnectionTable();
		mMessageHandler = messageHandler;
		mClass = (Class<IConnection>) clazz;
		mPluginID = pluginID;
	}
	
	protected IEnqueue getQueue()  {
		return mQueue;
	}
	
	protected IPluginID getPluginID()  {
		return mPluginID;
	}
	
	protected Class<IConnection> getConnectionClass()  {
		return mClass;
	}
	
	@Override
	public void send(IEvent event) {	
		IEndpointID target = null;
		
		if(IDirectedEvent.class.isAssignableFrom(event.getClass()))  {
			target = ((IDirectedEvent)event).getTargetID().getEndpoint();
		}
		else  {
			target = new DeviceID(ADVERTISEMENT, null);
		}
		
		if(target != null)  {
			if(target.equals(this.getPluginID().getDeviceID()))  {
				return;
			}
			
			List<IConnection> connections = this.getConnections(target);
			
			if(connections.isEmpty())  {				
				try {
					IConnection connection = this.getConnectionClass().getConstructor(IConnectionManager.class, String.class, int.class)
							.newInstance(this, target.getLocation().getAddress(), target.getLocation().getPort());
					addConnection(target, connection);
					connections.add(connection);
					connection.start();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}

			for(IConnection c : connections)  {
				if(DebugConstants.CONNECTION)
					Log.log(getClass(), "Sending event (" + event + ") to " + target.getID());
			
				c.send(event);
			}
		}
	}
	
	@Override
	public void receive(IEvent event) {
		if(event != null && mQueue != null)  {	
			int type = event.getType();
			IEndpointID source = null;
			
			if(DebugConstants.CONNECTION)
				Log.log(getClass(), "Received event (" + event + ")");
			
			//nur damit keine Nachrichten vom eigenen System weitergeleitet werden*****************
			if(type == IEvent.EVENT_REGISTRATION)  {
				source = ((IRegistrationEvent)event).getService().getId().getDeviceID();
			}
			else if(type == IEvent.EVENT_DEREGISTRATION)  {
				source = ((IServiceID) ((IDeregistrationEvent)event).getServiceID()).getDeviceID();
			}
			else if(type == IEvent.EVENT_ADVERTISEMENT)  {
				source = ((IAdvertisementEvent)event).getEndpointID();
			}
			else if(type == IEvent.EVENT_ANNOUNCEMENT)  {
				source = ((IAnnouncementEvent)event).getEndpointID();
			}
			else if(type == IEvent.EVENT_LOOKUP)  {
				source = ((IEndpointID) ((ILookupEvent)event).getSourceID());
			}
			else if(type == IEvent.EVENT_LOOKUPRESPONSE)  {
				source = ((IEndpointID) ((ILookupResponseEvent)event).getSourceID());
			}
			else if(type == IEvent.EVENT_APPLICATION)  {
				source = ((IServiceID) ((IApplicationEvent)event).getSourceID()).getDeviceID();
			}
			else if(type == IEvent.EVENT_APPLICATIONRESPONSE)  {
				source = ((IServiceID) ((IApplicationResponseEvent)event).getSourceID()).getDeviceID();
			}
			
			if(source != null)  {
				if(source.equals(mPluginID.getDeviceID()))  {
					return;
				}
			}
			//*****************************************************************
			
			event.setSource(mPluginID);
			
//			if(DebugConstants.CONNECTION)
//				Log.log(getClass(), "Received event (" + event + ")");
			
			mQueue.enqueue(event);
		}
	}
	
	public void addConnection(IEndpointID id, IConnection connection)  {
		this.mConnections.addEntry(((IDeviceID)id).getID(), 
				((IDeviceID)id).getLocation().toString(), connection);
	}
	
	public void deleteConnection(IEndpointID id)  {
		this.mConnections.deleteEntry(((IDeviceID)id).getID());
	}
	
	public void deleteConnection(IEndpointID id, boolean close)  {
		this.mConnections.deleteEntry(((IDeviceID)id).getID(), close);
	}
	
	public List<IConnection> getConnections(IEndpointID id)  {
		return this.mConnections.getConnection(((IDeviceID)id));
	}

	@Override
	public IMessageHandler getMessageHandler() {
		return mMessageHandler;
	}

	@Override
	public void stopConnections() {
		mConnections.reset(true);
	}
}
