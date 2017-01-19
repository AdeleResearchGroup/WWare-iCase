package de.mannheim.wifo2.iop.eventing.event.essential.impl;

//import java.util.List;

import de.mannheim.wifo2.iop.eventing.Event;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.eventing.event.essential.IApplicationResponseEvent;
import de.mannheim.wifo2.iop.identifier.IComponentID;
import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.service.functionality.ICall;

public class ApplicationResponseEvent extends Event 
		implements IApplicationResponseEvent {
	private static final long serialVersionUID = 8846972810284938292L;
	
	private IServiceID mInvocationSource;
	private IServiceID mInvocationTarget;
//	private List<IParameter> mResponse;
	private ICall mResponse;

	public ApplicationResponseEvent(IComponentID sourceID,
			Integer id, IServiceID invocationSource, IServiceID invocationTarget, 
//			List<IParameter> response,
			ICall response) {
		super(IEvent.EVENT_APPLICATIONRESPONSE, id, sourceID);
		mInvocationSource = invocationSource;
		mInvocationTarget = invocationTarget;
//		mResponse = response;
		mResponse = response;
	}

	@Override
	public IServiceID getSourceID() {
		return mInvocationSource;
	}

	@Override
	public IServiceID getTargetID() {
		return mInvocationTarget;
	}

//	@Override
//	public List<IParameter> getResponse() {
//		return mResponse;
//	}
	
	@Override
	public ICall getResponse() {
		return mResponse;
	}

	@Override
	public String toString()  {
		StringBuffer sb = new StringBuffer();
		sb.append("Event(");
		sb.append(getType() + ", ");
		sb.append(getID() + ", ");
		sb.append(mInvocationSource + ", ");
		sb.append(mInvocationTarget + ", ");
		sb.append(mResponse);
		sb.append(")");
		
		return sb.toString();
	}
}
