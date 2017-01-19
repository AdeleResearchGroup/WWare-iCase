package de.mannheim.wifo2.iop.eventing.event.essential;

//import java.util.List;

import de.mannheim.wifo2.iop.eventing.IDirectedEvent;
import de.mannheim.wifo2.iop.service.functionality.ICall;

public interface IApplicationResponseEvent extends IDirectedEvent  {
	public ICall getResponse();
}
