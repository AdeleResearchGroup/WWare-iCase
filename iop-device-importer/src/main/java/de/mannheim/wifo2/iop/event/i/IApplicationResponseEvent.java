package de.mannheim.wifo2.iop.event.i;

//import java.util.List;

import de.mannheim.wifo2.iop.event.IDirectedEvent;
import de.mannheim.wifo2.iop.service.functionality.ICall;

public interface IApplicationResponseEvent extends IDirectedEvent  {
	public ICall getResponse();
}
