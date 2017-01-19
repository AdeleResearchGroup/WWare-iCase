package de.mannheim.wifo2.iop.eventing.event.essential;

import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

public interface IRegistrationEvent extends IEvent {
	public IServiceDescription getService();
}
