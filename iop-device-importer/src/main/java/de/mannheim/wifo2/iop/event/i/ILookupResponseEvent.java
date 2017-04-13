package de.mannheim.wifo2.iop.event.i;

import java.util.List;

import de.mannheim.wifo2.iop.event.IDirectedEvent;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

public interface ILookupResponseEvent extends IDirectedEvent  {
	public List<? extends IServiceDescription> getServices();
}
