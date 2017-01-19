package de.mannheim.wifo2.iop.functions.discovery;

import de.mannheim.wifo2.iop.eventing.IChannel;
import de.mannheim.wifo2.iop.eventing.IEvent;

public interface ILookup extends IChannel<IEvent> {
	public void lookup(IEvent event);
	public void processResponse(IEvent event);
}
