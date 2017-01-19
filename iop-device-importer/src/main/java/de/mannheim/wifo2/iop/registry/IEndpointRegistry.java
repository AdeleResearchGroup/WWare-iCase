package de.mannheim.wifo2.iop.registry;

import de.mannheim.wifo2.iop.eventing.IChannel;
import de.mannheim.wifo2.iop.eventing.IEvent;
import de.mannheim.wifo2.iop.system.IStop;

public interface IEndpointRegistry extends IChannel<IEvent>, IStop  {
	
}
