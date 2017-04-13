package de.mannheim.wifo2.iop.event.impl;

import de.mannheim.wifo2.iop.event.Event;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.event.i.IApplicationEvent;
import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.service.functionality.ICall;

public class ApplicationEvent extends Event 
		implements IApplicationEvent {
	private static final long serialVersionUID = -3362104205802322300L;
	
	private IServiceID mInvocationSource;
	private IServiceID mInvocationTarget;
	private ICall mCall;

	public ApplicationEvent(IComponentID sourceID, Integer id,
			IServiceID invocationSource, IServiceID invocationTarget, ICall call) {
		super(IEvent.EVENT_APPLICATION, id, sourceID);
		mInvocationSource = invocationSource;
		mInvocationTarget = invocationTarget;
		mCall = call;
	}

	@Override
	public IServiceID getSourceID() {
		return mInvocationSource;
	}
	
	@Override
	public IServiceID getTargetID() {
		return mInvocationTarget;
	}
	
	@Override
	public ICall getCall()  {
		return mCall;
	}
	
	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("Event(");
		sb.append(getType() + ", ");
		sb.append(getID() + ", ");
		sb.append(mInvocationSource + ", ");
		sb.append(mInvocationTarget + ", ");
		sb.append(mCall);
		sb.append(")");
		
		return sb.toString();
	}
}
