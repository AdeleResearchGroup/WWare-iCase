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


import fr.liglab.adele.iop.device.api.IOPInvocationHandler;
import fr.liglab.adele.iop.device.api.IOPService;
import fr.liglab.adele.iop.services.api.LocationService;


/**
 * An proxy to an IOP XWare-based service implementing the LocationService interface
 * 
 * @author vega
 *
 */
@FunctionalExtender(contextServices = {LocationService.class, IOPService.class})
public class IOPLocationService implements LocationService, IOPService {


    
	@ContextEntity.State.Field(service=IOPService.class, state = IOPService.SERVICE_ID)
    private IServiceID remoteServiceId;

    @Requires(optional=false, proxy=false)
    private IOPInvocationHandler iopInvocationHandler;


    
	@Override
	public String[] getNearbyZones(String location) {
		
		try {
			String[] result = (String[]) iopInvocationHandler.invoke(remoteServiceId,
					new Call("getNearbyZones", Collections.singletonList(new Parameter("location", location, String.class)), String[].class),
					IOPInvocationHandler.TIMEOUT);
			
			return result;
		} catch (TimeoutException e) {}

		return null;
	}


	}
