package de.mannheim.wifo2.iop.functions.application;

import de.mannheim.wifo2.iop.connection.IConnectionManager;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.functions.application.IInvocation;
import de.mannheim.wifo2.iop.identifier.IPluginID;
import de.mannheim.wifo2.iop.util.debug.DebugConstants;
import de.mannheim.wifo2.iop.util.debug.Log;
import de.mannheim.wifo2.iop.util.i.IEnqueue;

public class Invocation implements IInvocation {
	private IPluginID mSystemID;
	@SuppressWarnings("unused")
	private IEnqueue mQueue;
	private IEnqueue mMediator;
	private IConnectionManager mConnectionManager;
	
	public Invocation(IEnqueue queue, IPluginID systemID, 
			IEnqueue mediator, IConnectionManager connectionManager) {
		mQueue = queue;
		mSystemID = systemID;
		mMediator = mediator;
		mConnectionManager = connectionManager;
	}
	
	@Override
	public void dispatch(IEvent event) {
		int type = event.getType();
		
		if(DebugConstants.INVOCATION)
			Log.log(getClass(), event+"");
		
		switch(type)  {
		case IEvent.EVENT_APPLICATION:
			invoke(event);
			break;
		case IEvent.EVENT_APPLICATIONRESPONSE:
			processResponse(event);
			break;
		}
	}

	@Override
	public void invoke(IEvent event) {
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
