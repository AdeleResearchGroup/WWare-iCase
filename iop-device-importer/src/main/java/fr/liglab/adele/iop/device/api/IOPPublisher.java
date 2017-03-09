package fr.liglab.adele.iop.device.api;

import java.util.List;

import de.mannheim.wifo2.iop.service.model.ICapability;

public interface IOPPublisher {

	public void publish(String id, String componentName, List<ICapability> capabilities, IOPInvocationHandler handler);
	
	public void unpublish(String id);
}
