package de.mannheim.wifo2.iop.functions.discovery;

import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.functions.matching.IServiceMatching;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.system.IEnqueue;

public class Lookup implements ILookup {
	
	private IPluginID mSystemID;
	@SuppressWarnings("unused")
	private IEnqueue mQueue;
	private IEnqueue mMediator;
	private IConnectionManager mConnectionManager;
	
	public Lookup(IEnqueue queue, IPluginID systemID, 
			IEnqueue mediator, IConnectionManager connectionManager, 
			IServiceMatching matching) {
		mQueue = queue;
		mSystemID = systemID;
		mMediator = mediator;
		mConnectionManager = connectionManager;
	}
	
	@Override
	public void dispatch(IEvent event) {
		int type = event.getType();

		switch(type)  {
		case IEvent.EVENT_LOOKUP:	
			lookup(event);
			break;
		case IEvent.EVENT_LOOKUPRESPONSE:
			processResponse(event);
			break;
		}
	}

	@Override
	public void lookup(IEvent event) {
		if(event.getSource().equals(mSystemID))  {
			mMediator.enqueue(event);
			
		}
		else  {
			mConnectionManager.send(event);
		}
	}

	@Override
	public void processResponse(IEvent event) {
		if(event.getSource().equals(mSystemID))  {
			mMediator.enqueue(event);
		}
		else  {
			mConnectionManager.send(event);
		}
	}
}
