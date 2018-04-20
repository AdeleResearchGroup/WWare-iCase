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
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.iop.device.api.IOPInvocationHandler;
import fr.liglab.adele.iop.device.api.IOPService;


/**
 * An proxy to an IOP XWare-based service implementing the BinaryLigth interface
 * 
 * @author vega
 *
 */
@FunctionalExtender(contextServices = {GenericDevice.class,Heater.class,IOPService.class})
public class IOPHeater implements GenericDevice, Heater, IOPService {


    /**
     * State
     */
	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

	@ContextEntity.State.Field(service=IOPService.class, state = IOPService.SERVICE_ID)
    private IServiceID remoteServiceId;

    @ContextEntity.State.Field(service = Heater.class,state = Heater.HEATER_POWER_LEVEL,directAccess=true, value="0.0")
    private double status;


    @Requires(optional=false, proxy=false)
    private IOPInvocationHandler iopInvocationHandler;


    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

	@Override
	public double getPowerLevel() {
		try {
			Double result = (Double) iopInvocationHandler.invoke(remoteServiceId,
					new Call("getPowerLevel", Collections.emptyList(), Double.TYPE),
					IOPInvocationHandler.TIMEOUT);
			
			if (result != null) {
				status = result.doubleValue();
			}
			
		} catch (TimeoutException e) {}

		return status;
	}


	@Override
	public void setPowerLevel(double status) {
		try {
			iopInvocationHandler.invoke(remoteServiceId,
					new Call("setPowerLevel", Collections.singletonList(new Parameter("status", status, Double.TYPE)), null),
					IOPInvocationHandler.TIMEOUT);
			
			this.status = status;
			
		} catch (TimeoutException e) {}
	}


}
