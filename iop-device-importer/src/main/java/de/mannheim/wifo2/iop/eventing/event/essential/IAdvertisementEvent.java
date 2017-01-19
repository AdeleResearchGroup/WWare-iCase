package de.mannheim.wifo2.iop.eventing.event.essential;

import java.util.List;

import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

public interface IAdvertisementEvent extends IEvent {
	public IEndpointID getEndpointID();
	public List<? extends IServiceDescription> getServices();
}
