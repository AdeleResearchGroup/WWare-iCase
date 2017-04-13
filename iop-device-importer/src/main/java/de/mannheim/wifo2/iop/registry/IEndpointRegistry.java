package de.mannheim.wifo2.iop.registry;

import de.mannheim.wifo2.iop.event.IChannel;
import de.mannheim.wifo2.iop.event.IEvent;
import de.mannheim.wifo2.iop.util.i.IStop;

public interface IEndpointRegistry extends IChannel<IEvent>, IStop  {
	
}
