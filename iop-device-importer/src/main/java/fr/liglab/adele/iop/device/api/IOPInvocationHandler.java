package fr.liglab.adele.iop.device.api;

import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.service.functionality.ICall;

public interface IOPInvocationHandler {

	public Object invoke(IServiceID target, ICall call);

}
