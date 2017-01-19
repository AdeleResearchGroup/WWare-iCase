package de.mannheim.wifo2.iop.eventing.event.essential;

import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.identifier.IID;

public interface IDeregistrationEvent extends IEvent {
	public IID getServiceID();
}
