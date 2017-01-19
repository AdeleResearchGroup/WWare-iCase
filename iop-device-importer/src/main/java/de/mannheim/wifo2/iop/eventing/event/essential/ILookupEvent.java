package de.mannheim.wifo2.iop.eventing.event.essential;

import de.mannheim.wifo2.iop.eventing.IDirectedEvent;
import de.mannheim.wifo2.iop.functions.matching.IMatchRequest;

public interface ILookupEvent extends IDirectedEvent {
	public IMatchRequest getRequest();
}
