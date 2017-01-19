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
package fr.liglab.adele.iop.device.proxies;

import java.util.Collections;

import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Requires;

import de.mannheim.wifo2.iop.identifier.IServiceID;
import de.mannheim.wifo2.iop.service.functionality.Call;
import de.mannheim.wifo2.iop.service.functionality.Parameter;
import fr.liglab.adele.cream.annotations.behavior.Behavior;
import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.helpers.location.provider.LocatedObjectBehaviorProvider;
import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.iop.device.api.IOPController;
import fr.liglab.adele.iop.device.api.IOPDevice;


@ContextEntity(services = {BinaryLight.class,IOPDevice.class})

@Behavior(id="LocatedBehavior",spec = LocatedObject.class,implem = LocatedObjectBehaviorProvider.class)

public class IOPLight implements  GenericDevice,  BinaryLight, IOPDevice  {

	@Property(name=fr.liglab.adele.cream.model.ContextEntity.CONTEXT_ENTITY_ID)
	private String proxyID;

	/**
     * STATES
     */
    @ContextEntity.State.Field(service = BinaryLight.class,state = BinaryLight.BINARY_LIGHT_POWER_STATUS,directAccess=true, value="false")
    private boolean status;

    @ContextEntity.State.Field(service = GenericDevice.class,state = GenericDevice.DEVICE_SERIAL_NUMBER)
    private String serialNumber;

    @ContextEntity.State.Field(service = IOPDevice.class,state = IOPDevice.SERVICE_ID)
    private IServiceID remoteService;
    
    @Requires(optional=false, proxy=false)
    private IOPController controller;

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }


	@Override
	public boolean getPowerStatus() {
		Boolean result = (Boolean) controller.invoke(remoteService, new Call("getPowerStatus", Collections.emptyList(), Boolean.class));
		if (result != null) {
			status = result.booleanValue();
			
		}
		return status;
	}


	@Override
	public void setPowerStatus(boolean status) {
		controller.invoke(remoteService, new Call("setPowerStatus", Collections.singletonList(new Parameter("status", status)), null));
		status = false;
	}


	@Override
	public void turnOff() {
		controller.invoke(remoteService, new Call("turnOff", Collections.emptyList(), null));
		status = false;
	}


	@Override
	public void turnOn() {
		controller.invoke(remoteService, new Call("turnOn", Collections.emptyList(), null));
		status = true;
	}


}
