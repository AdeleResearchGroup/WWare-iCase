package fr.liglab.adele.iop.device.api;

import java.util.List;

import de.mannheim.wifo2.iop.service.model.IFunctionality;

public interface IOPPublisher {

	public void publish(String id, String componentName, List<IFunctionality> functionalities, IOPInvocationHandler handler);
	
	public void unpublish(String id);
}
