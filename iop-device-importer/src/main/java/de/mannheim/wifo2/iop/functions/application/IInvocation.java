package de.mannheim.wifo2.iop.functions.application;

import de.mannheim.wifo2.iop.eventing.IChannel;
import de.mannheim.wifo2.iop.eventing.IEvent;

public interface IInvocation extends IChannel<IEvent>  {
	public void invoke(IEvent event);
	public void processResponse(IEvent event);
}
