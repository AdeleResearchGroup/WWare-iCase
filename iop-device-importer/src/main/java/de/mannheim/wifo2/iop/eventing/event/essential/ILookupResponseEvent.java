package de.mannheim.wifo2.iop.eventing.event.essential;

import java.util.List;

import de.mannheim.wifo2.iop.eventing.IDirectedEvent;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

public interface ILookupResponseEvent extends IDirectedEvent  {
	public List<? extends IServiceDescription> getServices();
}
