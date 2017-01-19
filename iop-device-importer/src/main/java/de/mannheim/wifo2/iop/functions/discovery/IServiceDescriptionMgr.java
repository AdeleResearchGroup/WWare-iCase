package de.mannheim.wifo2.iop.functions.discovery;

import de.mannheim.wifo2.iop.eventing.IChannel;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.repository.support.exchange.IServiceDesc;

public interface IServiceDescriptionMgr extends IChannel<IEvent> {
	public void addDescription(IServiceDesc description);
	public void processRequest(IEvent event);
	public void processResponse(IEvent event);	
}
