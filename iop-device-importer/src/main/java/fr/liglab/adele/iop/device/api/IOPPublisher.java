package fr.liglab.adele.iop.device.api;

import java.util.List;

import de.mannheim.wifo2.iop.service.model.ICapability;
import fr.liglab.adele.cream.annotations.ContextService;

public @ContextService  interface IOPPublisher {

	public void publish(String id, List<ICapability> capabilities, IOPInvocationHandler handler);
	
	public void unpublish(String id);
}
