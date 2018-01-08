package fr.liglab.adele.iop.device.icasa.proxies;

import java.util.Collections;
import java.util.concurrent.TimeoutException;

import org.apache.felix.ipojo.annotations.Requires;

import de.mannheim.wifo2.iop.identifier.IServiceID;

import de.mannheim.wifo2.iop.service.access.impl.Call;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

import fr.liglab.adele.icasa.device.GenericDevice;

import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.iop.device.api.IOPInvocationHandler;
import fr.liglab.adele.iop.device.api.IOPService;

@FunctionalExtender(contextServices = {GenericDevice.class,PresenceSensor.class,IOPService.class})
public class IOPPresenceSensor implements PresenceSensor, GenericDevice, IOPService {

	/**
     * State
     */
	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

	@ContextEntity.State.Field(service=IOPService.class, state = IOPService.SERVICE_ID)
    private IServiceID remoteServiceId;
	
    @ContextEntity.State.Field(service = PresenceSensor.class,state = PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE,directAccess=true, value="false")
    private boolean presence;
	

    @Requires(optional=false, proxy=false)
    private IOPInvocationHandler iopInvocationHandler;

    
	@Override
	public String getSerialNumber() {
		return serialNumber;
	}

	@Override
	public boolean getSensedPresence() {
		try {
			Boolean result = (Boolean) iopInvocationHandler.invoke(remoteServiceId, 
											new Call("getSensedPresence",Collections.emptyList(),Boolean.class),
											IOPInvocationHandler.TIMEOUT);
			if (result != null) {
				presence = result.booleanValue();		
			}
			
		} catch (TimeoutException e) {
		}
		
		return presence;
	}

}
