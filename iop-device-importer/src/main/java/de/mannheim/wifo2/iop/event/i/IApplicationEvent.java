package de.mannheim.wifo2.iop.event.i;

import de.mannheim.wifo2.iop.event.IDirectedEvent;
import de.mannheim.wifo2.iop.service.functionality.ICall;

public interface IApplicationEvent extends IDirectedEvent  {
	public ICall getCall();
}
