package de.mannheim.wifo2.iop.eventing.event.essential;

import de.mannheim.wifo2.iop.eventing.IDirectedEvent;
import de.mannheim.wifo2.iop.service.functionality.ICall;

public interface IApplicationEvent extends IDirectedEvent  {
	public ICall getCall();
}
