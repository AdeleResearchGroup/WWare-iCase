package de.mannheim.wifo2.iop.functions.discovery;

import java.util.List;
import java.util.Vector;

import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.ILookupEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.ILookupResponseEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.impl.LookupResponseEvent;
import de.mannheim.wifo2.iop.functions.discovery.ILookup;
import de.mannheim.wifo2.iop.functions.matching.IServiceMatching;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;
import de.mannheim.wifo2.iop.system.IEnqueue;

public class Lookup implements ILookup {
	
	private IPluginID mSystemID;
	@SuppressWarnings("unused")
	private IEnqueue mQueue;
	private IEnqueue mMediator;
	private IConnectionManager mConnectionManager;
//	private IServiceMatching mMatching;
	
	public Lookup(IEnqueue queue, IPluginID systemID, 
			IEnqueue mediator, IConnectionManager connectionManager, 
			IServiceMatching matching) {
		mQueue = queue;
		mSystemID = systemID;
		mMediator = mediator;
		mConnectionManager = connectionManager;
//		mMatching = matching;
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
			
//			ILookupEvent lookupEvent = (ILookupEvent) event;
//			List<? extends IServiceDescription> matchedServices = new Vector<>();
//			
//			ILookupResponseEvent responseEvent = new LookupResponseEvent(
//					mSystemID, lookupEvent.getID(), 
//					(IEndpointID)lookupEvent.getTargetID(), (IEndpointID)lookupEvent.getSourceID(), 
//					matchedServices);
//
//			mConnectionManager.send(responseEvent);
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
