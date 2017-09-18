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

import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.service.functionality.impl.Call;
import de.mannheim.wifo2.iop.service.functionality.impl.Parameter;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtender;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.iop.device.api.IOPInvocationHandler;
import fr.liglab.adele.iop.device.api.IOPService;
import org.apache.felix.ipojo.annotations.Requires;
import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;
import java.util.Collections;
import java.util.concurrent.TimeoutException;


/**
 * An proxy to an IOP XWare-based service implementing the BinaryLigth interface
 * 
 * @author vega
 *
 */
@FunctionalExtender(contextServices = {GenericDevice.class,Thermometer.class,IOPService.class})
public class IOPThermometer implements GenericDevice, Thermometer, IOPService {


    /**
     * State
     */
	@ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

	@ContextEntity.State.Field(service=IOPService.class, state = IOPService.SERVICE_ID)
    private IServiceID remoteServiceId;

    @ContextEntity.State.Field(service = Thermometer.class,state = Thermometer.THERMOMETER_CURRENT_TEMPERATURE,directAccess=true)
    private Quantity<Temperature> status;


    @Requires(optional=false, proxy=false)
    private IOPInvocationHandler iopInvocationHandler;


    @Override
    public String getSerialNumber() {
        return serialNumber;
    }


	@Override
	public Quantity<Temperature> getTemperature() {
		
		try {

			//System.out.println("***************** temperature call ***** ");

			Double result = (Double) iopInvocationHandler.invoke(remoteServiceId,
					new Call("getTemperature", Collections.emptyList(), Double.class),
					20000l);

			//System.out.println("***************** temperature received :"+result+" ***** ");
			if (result != null) {
				status = Quantities.getQuantity(result, Units.KELVIN);
			}
			
		} catch (TimeoutException e) {}

		return status;
	}



}
