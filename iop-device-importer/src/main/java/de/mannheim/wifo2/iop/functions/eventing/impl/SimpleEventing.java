package de.mannheim.wifo2.iop.functions.eventing.impl;

import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.functions.eventing.IEventing;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.util.i.IEnqueue;

public class SimpleEventing implements IEventing  {
	protected IPluginID mPluginID;
	protected IEnqueue mQueue;
	protected IEnqueue mMediator;
	protected IConnectionManager mConnectionManager;
	
	public SimpleEventing(IEnqueue queue, IPluginID pluginID, 
			IEnqueue mediator, IConnectionManager connectionManager) {
		mQueue = queue;
		mPluginID = pluginID;
		mMediator = mediator;
		mConnectionManager = connectionManager;
	}
	
	@Override
	public void dispatch(IEvent event) {
		int type = event.getType();
		
		switch(type)  {
			case IEvent.EVENT_EVENTING:
				if(event.getSource().equals(mPluginID))  {
					processNotification(event);
				}
				else  {
					notify(event);
				}
			
				break;
		}
	}

	@Override
	public void notify(IEvent event) {
		//TODO implement, event comes from mediator
	}

	@Override
	public void processNotification(IEvent event) {
		//TODO implement, event comes from local service
	}
}
