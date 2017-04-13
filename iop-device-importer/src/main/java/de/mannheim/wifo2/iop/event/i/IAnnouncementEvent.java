package de.mannheim.wifo2.iop.event.i;

import java.util.List;

import de.mannheim.wifo2.iop.event.IDirectedEvent;
import de.mannheim.wifo2.iop.identifier.IEndpointID;
import de.mannheim.wifo2.iop.service.model.IServiceDescription;

public interface IAnnouncementEvent extends IDirectedEvent  {
	public IEndpointID getSourceID();
	public IEndpointID getTargetID();
	public List<? extends IServiceDescription> getServices();
	public long getTimeToLive();
}
