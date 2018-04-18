package fr.liglab.adele.iop.device.api;

import java.util.List;
import java.util.Map;

import de.mannheim.wifo2.iop.service.model.IFunctionality;

public interface IOPPublisher {

	public void publish(String id, String componentName, List<IFunctionality> functionalities, Map<String,?> properties, IOPInvocationHandler handler);

	public void unpublish(String id);
}
