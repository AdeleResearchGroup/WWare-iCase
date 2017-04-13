package de.mannheim.wifo2.iop.event;

import de.mannheim.wifo2.iop.identifier.IID;

public interface IDirectedEvent extends IEvent {
	public IID getSourceID();
	public IID getTargetID();
}
