/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.iop.device.icasa.proxies;

import java.util.Collections;
import java.util.concurrent.TimeoutException;

import org.apache.felix.ipojo.annotations.Requires;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;

import de.mannheim.wifo2.iop.identifier.IServiceID;

import de.mannheim.wifo2.iop.service.access.impl.Call;
import de.mannheim.wifo2.iop.service.access.impl.Parameter;


import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.iop.device.api.IOPInvocationHandler;
import fr.liglab.adele.iop.device.api.IOPService;


/**
 * An proxy to an IOP XWare-based service implementing the BinaryLigth interface
 * 
 * @author vega
 *
 */
@FunctionalExtender(contextServices = {GenericDevice.class,BinaryLight.class,IOPService.class})
public class IOPBinaryLight implements GenericDevice, BinaryLight, IOPService {


    /**
     * State
     */
	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

	@ContextEntity.State.Field(service=IOPService.class, state = IOPService.SERVICE_ID)
    private IServiceID remoteServiceId;

    @ContextEntity.State.Field(service = BinaryLight.class,state = BinaryLight.BINARY_LIGHT_POWER_STATUS,directAccess=true, value="false")
    private boolean status;


    @Requires(optional=false, proxy=false)
    private IOPInvocationHandler iopInvocationHandler;


    @Override
    public String getSerialNumber() {
        return serialNumber;
    }


	@Override
	public boolean getPowerStatus() {
		
		try {
			Boolean result = (Boolean) iopInvocationHandler.invoke(remoteServiceId,
					new Call("getPowerStatus", Collections.emptyList(), Boolean.class),
					IOPInvocationHandler.TIMEOUT);
			
			if (result != null) {
				status = result.booleanValue();
			}
			
		} catch (TimeoutException e) {}

		return status;
	}


	@Override
	public void setPowerStatus(boolean status) {
		try {
			iopInvocationHandler.invoke(remoteServiceId,
					new Call("setPowerStatus", Collections.singletonList(new Parameter("status", status)), null),
					IOPInvocationHandler.TIMEOUT);
			
			this.status = status;
			
		} catch (TimeoutException e) {}
	}


	@Override
	public void turnOff() {
		try {
			iopInvocationHandler.invoke(remoteServiceId,
					new Call("setPowerStatus", Collections.singletonList(new Parameter("status", false)), null),
					IOPInvocationHandler.TIMEOUT);
			
			status = false;
			
		} catch (TimeoutException e) {}
	}


	@Override
	public void turnOn() {
		try {
			iopInvocationHandler.invoke(remoteServiceId,
					new Call("setPowerStatus", Collections.singletonList(new Parameter("status", true)), null),
					IOPInvocationHandler.TIMEOUT);
			
			status = true;
			
		} catch (TimeoutException e) {}
	}


}
