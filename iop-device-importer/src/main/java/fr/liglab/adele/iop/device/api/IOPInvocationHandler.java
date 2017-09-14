package fr.liglab.adele.iop.device.api;

import java.util.concurrent.TimeoutException;

import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.service.functionality.ICall;

public interface IOPInvocationHandler {

	/**
	 * The default time out for invocation dispatch
	 */
	public static final long TIMEOUT = 1000l;
	
	/**
	 * invokes the specified method of the referenced service
	 */
	public Object invoke(IServiceID target, ICall call, long timeout) throws TimeoutException;

}
