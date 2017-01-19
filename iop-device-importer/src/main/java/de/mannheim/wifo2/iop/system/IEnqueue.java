package de.mannheim.wifo2.iop.system;

import de.mannheim.wifo2.iop.eventing.IEvent;

public interface IEnqueue {
	public void enqueue(IEvent message);
}
