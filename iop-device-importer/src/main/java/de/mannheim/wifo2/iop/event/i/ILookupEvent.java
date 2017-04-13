package de.mannheim.wifo2.iop.event.i;

import de.mannheim.wifo2.iop.event.IDirectedEvent;
import de.mannheim.wifo2.iop.plugin.function.matching.IMatchRequest;

public interface ILookupEvent extends IDirectedEvent {
	public IMatchRequest getRequest();
}
